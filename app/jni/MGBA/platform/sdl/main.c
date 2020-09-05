/* Copyright (c) 2013-2015 Jeffrey Pfau
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
#include "main.h"

#include <mgba/internal/debugger/cli-debugger.h>

#ifdef USE_GDB_STUB
#include <mgba/internal/debugger/gdb-stub.h>
#endif
#ifdef USE_EDITLINE
#include "feature/editline/cli-el-backend.h"
#endif
#ifdef ENABLE_SCRIPTING
#include <mgba/core/scripting.h>

#ifdef ENABLE_PYTHON
#include "platform/python/engine.h"
#endif
#endif

#include <mgba/core/cheats.h>
#include <mgba/core/core.h>
#include <mgba/core/config.h>
#include <mgba/core/input.h>
#include <mgba/core/serialize.h>
#include <mgba/core/thread.h>
#include <mgba/internal/gba/input.h>

#include <mgba/feature/commandline.h>
#include <mgba-util/vfs.h>

#include <SDL.h>

#include <errno.h>
#include <signal.h>
#include <jni.h>
#include <include/mgba/internal/gba/cheats.h>
#include <include/mgba/gba/interface.h>

#define PORT "sdl"

struct mCoreThread global_thread = {0};
struct mSDLRenderer* global_renderer = NULL;
static bool mSDLInit(struct mSDLRenderer* renderer);
static void mSDLDeinit(struct mSDLRenderer* renderer);

static int mSDLRun(struct mSDLRenderer* renderer, struct mArguments* args);

static struct VFile* _state = NULL;

static void _loadState(struct mCoreThread* thread) {
	mCoreLoadStateNamed(thread->core, _state, SAVESTATE_RTC);
}
//作弊
struct mCheatDevice*  m_device = NULL;
//struct mCheatSet* m_cheatSet = NULL;
//添加作弊码
bool SDL_addCheatCode(char*chat_name,char*chat_str,bool enable) {
	int codeType = GBA_CHEAT_AUTODETECT;
	struct mCheatSet* m_cheatSet = m_device->createSet(m_device, chat_name);
    m_cheatSet->enabled = enable;
	bool isOK = mCheatAddLine(m_cheatSet,chat_str, codeType);
	if(!isOK){
		return isOK;
	}
	mCheatAddSet(m_device, m_cheatSet);
    return true;
}

//获取当前行的cheat
struct mCheatSet* getItemCheat(int index){
	return *mCheatSetsGetPointer(&m_device->cheats, index);
}
//停用或者开启一个cheat
void SDL_enableCheat(int index,bool enabled){
	struct mCheatSet* set = *mCheatSetsGetPointer(&m_device->cheats, index);
	set->enabled = enabled;
}
//移除一个cheat
void SDL_removeCheat(int index){
	struct mCheatSet* set = *mCheatSetsGetPointer(&m_device->cheats, index);
	mCheatRemoveSet(m_device, set);
	mCheatSetDeinit(set);
	//mCheatAutosave(m_device);
}
JNIEnv *_env = NULL;
//存储JNIEnv
void SDL_onSetJNIEnv(JNIEnv *env){
	_env = env;
}

//保存读取数据
void SDL_onSlotNum(int key, bool isSave){
	onSlotKey(&global_thread,key,isSave);
}

//设置是否全屏
void SDL_onScreenSize(bool isFull,int width,int height){
    if(global_renderer->width!=GBA_VIDEO_HORIZONTAL_PIXELS) {
        global_renderer->viewportWidth = width;
        global_renderer->viewportHeight = height;
        float SCREENT_X = 256.0f / global_renderer->width;
        float SCREENT_Y = 224.0f / global_renderer->height;
        SCREENT_RECT.w = (global_renderer->viewportWidth * SCREENT_X);
        SCREENT_RECT.h = (global_renderer->viewportHeight * SCREENT_Y);
    }else{
        SCREENT_RECT.w = width;
        SCREENT_RECT.h = height;
    }
   // SCREENT_RECT = {0, 0, (width * SCREENT_X), (height * SCREENT_Y)};
}

//由java回调保存响应按键
void SDL_onDataKey(int key, bool down){
	bool isSpecial =  onKeySpecial(_env,&global_thread,key,down);
	if(isSpecial){
		return;
	}
	key = mInputMapKey(global_renderer->player.bindings, SDL_BINDING_KEY, key);
	if(down){
		onKeyDown(&global_thread,key);
	}else{
		onKeyUp(&global_thread,key);
	}

}

int main(int argc, char** argv) {
	struct mSDLRenderer renderer = {0};

	struct mCoreOptions opts = {
		.useBios = true,
		.rewindEnable = true,
		.rewindBufferCapacity = 600,
		.audioBuffers = 1024,
		.videoSync = false,
		.audioSync = true,
		.volume = 0x100,
	};

	struct mArguments args;
	struct mGraphicsOpts graphicsOpts;

	struct mSubParser subparser;
	initParserForGraphics(&subparser, &graphicsOpts);
	bool parsed = parseArguments(&args, argc, argv, &subparser);

	if (!args.fname && !args.showVersion) {
		parsed = false;
	}
	if (!parsed || args.showHelp) {
		usage(argv[0], subparser.usage);
		freeArguments(&args);
		return !parsed;
	}
	if (args.showVersion) {
		version(argv[0]);
		freeArguments(&args);
		return 0;
	}

	renderer.core = mCoreFind(args.fname);
	if (!renderer.core) {
		printf("Could not run game. Are you sure the file exists and is a compatible game?\n");
		freeArguments(&args);
		return 1;
	}
	global_renderer = &renderer;

	if (!renderer.core->init(renderer.core)) {
		freeArguments(&args);
		return 1;
	}

	renderer.core->desiredVideoDimensions(renderer.core, &renderer.width, &renderer.height);
#ifdef BUILD_GL
	mSDLGLCreate(&renderer);
#elif defined(BUILD_GLES2) || defined(USE_EPOXY)
	mSDLGLES2Create(&renderer);
#else
	mSDLSWCreate(&renderer);
#endif

	renderer.ratio = graphicsOpts.multiplier;
	if (renderer.ratio == 0) {
		renderer.ratio = 1;
	}
	opts.width = renderer.width * renderer.ratio;
	opts.height = renderer.height * renderer.ratio;

	struct mCheatDevice* device = NULL;
	if (args.cheatsFile && (device = renderer.core->cheatDevice(renderer.core))) {
		struct VFile* vf = VFileOpen(args.cheatsFile, O_RDONLY);
		if (vf) {
			mCheatDeviceClear(device);
			mCheatParseFile(device, vf);
			vf->close(vf);
		}
	}

//	if(device!=NULL){
//		m_device = device;
//	}
    m_device = renderer.core->cheatDevice(renderer.core);

	mInputMapInit(&renderer.core->inputMap, &GBAInputInfo);
	mCoreInitConfig(renderer.core, PORT);
	applyArguments(&args, &subparser, &renderer.core->config);

	mCoreConfigLoadDefaults(&renderer.core->config, &opts);
	mCoreLoadConfig(renderer.core);

	renderer.viewportWidth = renderer.core->opts.width;
	renderer.viewportHeight = renderer.core->opts.height;
	renderer.player.fullscreen = renderer.core->opts.fullscreen;
	renderer.player.windowUpdated = 0;

	renderer.lockAspectRatio = renderer.core->opts.lockAspectRatio;
	renderer.lockIntegerScaling = renderer.core->opts.lockIntegerScaling;
	renderer.interframeBlending = renderer.core->opts.interframeBlending;
	renderer.filter = renderer.core->opts.resampleVideo;

	if (!mSDLInit(&renderer)) {
		freeArguments(&args);
		mCoreConfigDeinit(&renderer.core->config);
		renderer.core->deinit(renderer.core);
		return 1;
	}

	renderer.player.bindings = &renderer.core->inputMap;
	mSDLInitBindingsGBA(&renderer.core->inputMap);
	mSDLInitEvents(&renderer.events);
	mSDLEventsLoadConfig(&renderer.events, mCoreConfigGetInput(&renderer.core->config));
	mSDLAttachPlayer(&renderer.events, &renderer.player);
	mSDLPlayerLoadConfig(&renderer.player, mCoreConfigGetInput(&renderer.core->config));

#if SDL_VERSION_ATLEAST(2, 0, 0)
	renderer.core->setPeripheral(renderer.core, mPERIPH_RUMBLE, &renderer.player.rumble.d);
#endif

	int ret;

	// TODO: Use opts and config
	ret = mSDLRun(&renderer, &args);
	mSDLDetachPlayer(&renderer.events, &renderer.player);
	mInputMapDeinit(&renderer.core->inputMap);

	if (device) {
		mCheatDeviceDestroy(device);
	}

	mSDLDeinit(&renderer);

	freeArguments(&args);
	mCoreConfigFreeOpts(&opts);
	mCoreConfigDeinit(&renderer.core->config);
	renderer.core->deinit(renderer.core);

	return ret;
}

#if defined(_WIN32) && !defined(_UNICODE)
#include <mgba-util/string.h>

int wmain(int argc, wchar_t** argv) {
	char** argv8 = malloc(sizeof(char*) * argc);
	int i;
	for (i = 0; i < argc; ++i) {
		argv8[i] = utf16to8((uint16_t*) argv[i], wcslen(argv[i]) * 2);
	}
	int ret = main(argc, argv8);
	for (i = 0; i < argc; ++i) {
		free(argv8[i]);
	}
	free(argv8);
	return ret;
}
#endif

int mSDLRun(struct mSDLRenderer* renderer, struct mArguments* args) {
	struct mCoreThread thread = {
		.core = renderer->core
	};
	if (!mCoreLoadFile(renderer->core, args->fname)) {
		return 1;
	}
	mCoreAutoloadSave(renderer->core);
	mCoreAutoloadCheats(renderer->core);
#ifdef ENABLE_SCRIPTING
	struct mScriptBridge* bridge = mScriptBridgeCreate();
#ifdef ENABLE_PYTHON
	mPythonSetup(bridge);
#endif
#endif

#ifdef USE_DEBUGGERS
	struct mDebugger* debugger = mDebuggerCreate(args->debuggerType, renderer->core);
	if (debugger) {
#ifdef USE_EDITLINE
		if (args->debuggerType == DEBUGGER_CLI) {
			struct CLIDebugger* cliDebugger = (struct CLIDebugger*) debugger;
			CLIDebuggerAttachBackend(cliDebugger, CLIDebuggerEditLineBackendCreate());
		}
#endif
		mDebuggerAttach(debugger, renderer->core);
		mDebuggerEnter(debugger, DEBUGGER_ENTER_MANUAL, NULL);
 #ifdef ENABLE_SCRIPTING
		mScriptBridgeSetDebugger(bridge, debugger);
#endif
	}
#endif

	if (args->patch) {
		struct VFile* patch = VFileOpen(args->patch, O_RDONLY);
		if (patch) {
			renderer->core->loadPatch(renderer->core, patch);
		}
	} else {
		mCoreAutoloadPatch(renderer->core);
	}

	renderer->audio.samples = renderer->core->opts.audioBuffers;
	renderer->audio.sampleRate = 44100;

	bool didFail = !mCoreThreadStart(&thread);
	if (!didFail) {
		global_thread = thread;
#if SDL_VERSION_ATLEAST(2, 0, 0)
		renderer->core->desiredVideoDimensions(renderer->core, &renderer->width, &renderer->height);
		unsigned width = renderer->width * renderer->ratio;
		unsigned height = renderer->height * renderer->ratio;
		if (width != (unsigned) renderer->viewportWidth && height != (unsigned) renderer->viewportHeight) {
			SDL_SetWindowSize(renderer->window, width, height);
			renderer->player.windowUpdated = 1;
		}
		mSDLSetScreensaverSuspendable(&renderer->events, renderer->core->opts.suspendScreensaver);
		mSDLSuspendScreensaver(&renderer->events);
#endif
		if (mSDLInitAudio(&renderer->audio, &thread)) {
			if (args->savestate) {
				struct VFile* state = VFileOpen(args->savestate, O_RDONLY);
				if (state) {
					_state = state;
					mCoreThreadRunFunction(&thread, _loadState);
					_state = NULL;
					state->close(state);
				}
			}
			renderer->runloop(renderer, &thread);
			mSDLPauseAudio(&renderer->audio);
			if (mCoreThreadHasCrashed(&thread)) {
				didFail = true;
				printf("The game crashed!\n");
			}
		} else {
			didFail = true;
			printf("Could not initialize audio.\n");
		}
#if SDL_VERSION_ATLEAST(2, 0, 0)
		mSDLResumeScreensaver(&renderer->events);
		mSDLSetScreensaverSuspendable(&renderer->events, false);
#endif

		mCoreThreadJoin(&thread);
	} else {
		printf("Could not run game. Are you sure the file exists and is a compatible game?\n");
	}
	renderer->core->unloadROM(renderer->core);

#ifdef ENABLE_SCRIPTING
	mScriptBridgeDestroy(bridge);
#endif

	return didFail;
}

static bool mSDLInit(struct mSDLRenderer* renderer) {
	if (SDL_Init(SDL_INIT_VIDEO) < 0) {
		printf("Could not initialize video: %s\n", SDL_GetError());
		return false;
	}

	return renderer->init(renderer);
}

static void mSDLDeinit(struct mSDLRenderer* renderer) {
	mSDLDeinitEvents(&renderer->events);
	mSDLDeinitAudio(&renderer->audio);
#if SDL_VERSION_ATLEAST(2, 0, 0)
	SDL_DestroyWindow(renderer->window);
#endif

	renderer->deinit(renderer);

	SDL_Quit();
}
