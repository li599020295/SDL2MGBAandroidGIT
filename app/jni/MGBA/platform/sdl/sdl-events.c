/* Copyright (c) 2013-2014 Jeffrey Pfau
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
#include "sdl-events.h"

#include <mgba/core/core.h>
#include <mgba/core/input.h>
#include <mgba/core/serialize.h>
#include <mgba/core/thread.h>
#include <mgba/debugger/debugger.h>
#include <mgba/internal/gba/input.h>
#include <mgba-util/configuration.h>
#include <mgba-util/formatting.h>
#include <mgba-util/vfs.h>

#if SDL_VERSION_ATLEAST(2, 0, 0) && defined(__APPLE__)
#define GUI_MOD KMOD_GUI
#else
#define GUI_MOD KMOD_CTRL
#endif

#define GYRO_STEPS 100
#define RUMBLE_PWM 16
#define RUMBLE_STEPS 2

mLOG_DEFINE_CATEGORY(SDL_EVENTS, "SDL Events", "platform.sdl.events");

DEFINE_VECTOR(SDL_JoystickList, struct SDL_JoystickCombo);

#if SDL_VERSION_ATLEAST(2, 0, 0)
static void _mSDLSetRumble(struct mRumble* rumble, int enable);
#endif
static int32_t _mSDLReadTiltX(struct mRotationSource* rumble);
static int32_t _mSDLReadTiltY(struct mRotationSource* rumble);
static int32_t _mSDLReadGyroZ(struct mRotationSource* rumble);
static void _mSDLRotationSample(struct mRotationSource* source);

bool mSDLInitEvents(struct mSDLEvents* context) {
#if SDL_VERSION_ATLEAST(2, 0, 4)
	SDL_SetHint(SDL_HINT_NO_SIGNAL_HANDLERS, "1");
#endif
	if (SDL_InitSubSystem(SDL_INIT_JOYSTICK) < 0) {
		mLOG(SDL_EVENTS, ERROR, "SDL joystick initialization failed: %s", SDL_GetError());
	}

#if SDL_VERSION_ATLEAST(2, 0, 0)
	SDL_SetHint(SDL_HINT_JOYSTICK_ALLOW_BACKGROUND_EVENTS, "1");
	if (SDL_InitSubSystem(SDL_INIT_HAPTIC) < 0) {
		mLOG(SDL_EVENTS, ERROR, "SDL haptic initialization failed: %s", SDL_GetError());
	}
	if (SDL_InitSubSystem(SDL_INIT_VIDEO) < 0) {
		mLOG(SDL_EVENTS, ERROR, "SDL video initialization failed: %s", SDL_GetError());
	}
#endif
	//这个会导致摇杆生效和按键冲突先注释！
	//SDL_JoystickEventState(SDL_ENABLE);
	//强制等于0
	int nJoysticks = 0;//SDL_NumJoysticks();
	SDL_JoystickListInit(&context->joysticks, nJoysticks);
	if (nJoysticks > 0) {
		mSDLUpdateJoysticks(context, NULL);
		// Some OSes don't do hotplug detection
		if (!SDL_JoystickListSize(&context->joysticks)) {
			int i;
			for (i = 0; i < nJoysticks; ++i) {
				SDL_Joystick* sdlJoystick = SDL_JoystickOpen(i);
				if (!sdlJoystick) {
					continue;
				}
				struct SDL_JoystickCombo* joystick = SDL_JoystickListAppend(&context->joysticks);
				joystick->joystick = sdlJoystick;
				joystick->index = SDL_JoystickListSize(&context->joysticks) - 1;
#if SDL_VERSION_ATLEAST(2, 0, 0)
				joystick->id = SDL_JoystickInstanceID(joystick->joystick);
				joystick->haptic = SDL_HapticOpenFromJoystick(joystick->joystick);
#else
				joystick->id = SDL_JoystickIndex(joystick->joystick);
#endif
			}
		}
	}

	context->playersAttached = 0;

	size_t i;
	for (i = 0; i < MAX_PLAYERS; ++i) {
		context->preferredJoysticks[i] = 0;
	}

#if !SDL_VERSION_ATLEAST(2, 0, 0)
	SDL_EnableKeyRepeat(SDL_DEFAULT_REPEAT_DELAY, SDL_DEFAULT_REPEAT_INTERVAL);
#else
	context->screensaverSuspendDepth = 0;
#endif
	return true;
}

void mSDLDeinitEvents(struct mSDLEvents* context) {
	size_t i;
	for (i = 0; i < SDL_JoystickListSize(&context->joysticks); ++i) {
		struct SDL_JoystickCombo* joystick = SDL_JoystickListGetPointer(&context->joysticks, i);
#if SDL_VERSION_ATLEAST(2, 0, 0)
		SDL_HapticClose(joystick->haptic);
#endif
		SDL_JoystickClose(joystick->joystick);
	}
	SDL_JoystickListDeinit(&context->joysticks);
	SDL_QuitSubSystem(SDL_INIT_JOYSTICK);
}

void mSDLEventsLoadConfig(struct mSDLEvents* context, const struct Configuration* config) {
	context->preferredJoysticks[0] = mInputGetPreferredDevice(config, "gba", SDL_BINDING_BUTTON, 0);
	context->preferredJoysticks[1] = mInputGetPreferredDevice(config, "gba", SDL_BINDING_BUTTON, 1);
	context->preferredJoysticks[2] = mInputGetPreferredDevice(config, "gba", SDL_BINDING_BUTTON, 2);
	context->preferredJoysticks[3] = mInputGetPreferredDevice(config, "gba", SDL_BINDING_BUTTON, 3);
}

void mSDLInitBindingsGBA(struct mInputMap* inputMap) {
#ifdef BUILD_PANDORA
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_PAGEDOWN, GBA_KEY_A);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_END, GBA_KEY_B);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_RSHIFT, GBA_KEY_L);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_RCTRL, GBA_KEY_R);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_LALT, GBA_KEY_START);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_LCTRL, GBA_KEY_SELECT);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_UP, GBA_KEY_UP);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_DOWN, GBA_KEY_DOWN);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_LEFT, GBA_KEY_LEFT);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_RIGHT, GBA_KEY_RIGHT);
#else
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_x, GBA_KEY_A);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_z, GBA_KEY_B);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_a, GBA_KEY_L);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_s, GBA_KEY_R);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_RETURN, GBA_KEY_START);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_BACKSPACE, GBA_KEY_SELECT);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_UP, GBA_KEY_UP);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_DOWN, GBA_KEY_DOWN);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_LEFT, GBA_KEY_LEFT);
	mInputBindKey(inputMap, SDL_BINDING_KEY, SDLK_RIGHT, GBA_KEY_RIGHT);
#endif

	struct mInputAxis description = { GBA_KEY_RIGHT, GBA_KEY_LEFT, 0x4000, -0x4000 };
	mInputBindAxis(inputMap, SDL_BINDING_BUTTON, 0, &description);
	description = (struct mInputAxis) { GBA_KEY_DOWN, GBA_KEY_UP, 0x4000, -0x4000 };
	mInputBindAxis(inputMap, SDL_BINDING_BUTTON, 1, &description);

	mInputBindHat(inputMap, SDL_BINDING_BUTTON, 0, &GBAInputInfo.hat);
}

bool mSDLAttachPlayer(struct mSDLEvents* events, struct mSDLPlayer* player) {
	player->joystick = 0;

	if (events->playersAttached >= MAX_PLAYERS) {
		return false;
	}

#if SDL_VERSION_ATLEAST(2, 0, 0)
	player->rumble.d.setRumble = _mSDLSetRumble;
	CircleBufferInit(&player->rumble.history, RUMBLE_PWM);
	player->rumble.level = 0;
	player->rumble.activeLevel = 0;
	player->rumble.p = player;
#endif

	player->rotation.d.readTiltX = _mSDLReadTiltX;
	player->rotation.d.readTiltY = _mSDLReadTiltY;
	player->rotation.d.readGyroZ = _mSDLReadGyroZ;
	player->rotation.d.sample = _mSDLRotationSample;
	player->rotation.axisX = 2;
	player->rotation.axisY = 3;
	player->rotation.gyroSensitivity = 2.2e9f;
	player->rotation.gyroX = 0;
	player->rotation.gyroY = 1;
	player->rotation.zDelta = 0;
	CircleBufferInit(&player->rotation.zHistory, sizeof(float) * GYRO_STEPS);
	player->rotation.p = player;

	player->playerId = events->playersAttached;
	events->players[player->playerId] = player;
	size_t firstUnclaimed = SIZE_MAX;
	size_t index = SIZE_MAX;

	size_t i;
	for (i = 0; i < SDL_JoystickListSize(&events->joysticks); ++i) {
		bool claimed = false;

		int p;
		for (p = 0; p < events->playersAttached; ++p) {
			if (events->players[p]->joystick == SDL_JoystickListGetPointer(&events->joysticks, i)) {
				claimed = true;
				break;
			}
		}
		if (claimed) {
			continue;
		}

		if (firstUnclaimed == SIZE_MAX) {
			firstUnclaimed = i;
		}

#if SDL_VERSION_ATLEAST(2, 0, 0)
		char joystickName[34] = {0};
		SDL_JoystickGetGUIDString(SDL_JoystickGetGUID(SDL_JoystickListGetPointer(&events->joysticks, i)->joystick), joystickName, sizeof(joystickName));
#else
		const char* joystickName = SDL_JoystickName(SDL_JoystickIndex(SDL_JoystickListGetPointer(&events->joysticks, i)->joystick));
		if (!joystickName) {
			continue;
		}
#endif
		if (events->preferredJoysticks[player->playerId] && strcmp(events->preferredJoysticks[player->playerId], joystickName) == 0) {
			index = i;
			break;
		}
	}

	if (index == SIZE_MAX && firstUnclaimed != SIZE_MAX) {
		index = firstUnclaimed;
	}

	if (index != SIZE_MAX) {
		player->joystick = SDL_JoystickListGetPointer(&events->joysticks, index);

#if SDL_VERSION_ATLEAST(2, 0, 0)
		if (player->joystick->haptic) {
			SDL_HapticRumbleInit(player->joystick->haptic);
		}
#endif
	}

	++events->playersAttached;
	return true;
}

void mSDLDetachPlayer(struct mSDLEvents* events, struct mSDLPlayer* player) {
	if (player != events->players[player->playerId]) {
		return;
	}
	int i;
	for (i = player->playerId; i < events->playersAttached; ++i) {
		if (i + 1 < MAX_PLAYERS) {
			events->players[i] = events->players[i + 1];
		}
		if (i < events->playersAttached - 1) {
			events->players[i]->playerId = i;
		}
	}
	--events->playersAttached;
	CircleBufferDeinit(&player->rotation.zHistory);
#if SDL_VERSION_ATLEAST(2, 0, 0)
	CircleBufferDeinit(&player->rumble.history);
#endif
}

void mSDLPlayerLoadConfig(struct mSDLPlayer* context, const struct Configuration* config) {
	mInputMapLoad(context->bindings, SDL_BINDING_KEY, config);
	if (context->joystick) {
		mInputMapLoad(context->bindings, SDL_BINDING_BUTTON, config);
#if SDL_VERSION_ATLEAST(2, 0, 0)
		char name[34] = {0};
		SDL_JoystickGetGUIDString(SDL_JoystickGetGUID(context->joystick->joystick), name, sizeof(name));
#else
		const char* name = SDL_JoystickName(SDL_JoystickIndex(context->joystick->joystick));
		if (!name) {
			return;
		}
#endif
		mInputProfileLoad(context->bindings, SDL_BINDING_BUTTON, config, name);

		const char* value;
		char* end;
		int numAxes = SDL_JoystickNumAxes(context->joystick->joystick);
		int axis;
		value = mInputGetCustomValue(config, "gba", SDL_BINDING_BUTTON, "tiltAxisX", name);
		if (value) {
			axis = strtol(value, &end, 0);
			if (axis >= 0 && axis < numAxes && end && !*end) {
				context->rotation.axisX = axis;
			}
		}
		value = mInputGetCustomValue(config, "gba", SDL_BINDING_BUTTON, "tiltAxisY", name);
		if (value) {
			axis = strtol(value, &end, 0);
			if (axis >= 0 && axis < numAxes && end && !*end) {
				context->rotation.axisY = axis;
			}
		}
		value = mInputGetCustomValue(config, "gba", SDL_BINDING_BUTTON, "gyroAxisX", name);
		if (value) {
			axis = strtol(value, &end, 0);
			if (axis >= 0 && axis < numAxes && end && !*end) {
				context->rotation.gyroX = axis;
			}
		}
		value = mInputGetCustomValue(config, "gba", SDL_BINDING_BUTTON, "gyroAxisY", name);
		if (value) {
			axis = strtol(value, &end, 0);
			if (axis >= 0 && axis < numAxes && end && !*end) {
				context->rotation.gyroY = axis;
			}
		}
		value = mInputGetCustomValue(config, "gba", SDL_BINDING_BUTTON, "gyroSensitivity", name);
		if (value) {
			float sensitivity = strtof_u(value, &end);
			if (end && !*end) {
				context->rotation.gyroSensitivity = sensitivity;
			}
		}
	}
}

void mSDLPlayerSaveConfig(const struct mSDLPlayer* context, struct Configuration* config) {
	if (context->joystick) {
#if SDL_VERSION_ATLEAST(2, 0, 0)
		char name[34] = {0};
		SDL_JoystickGetGUIDString(SDL_JoystickGetGUID(context->joystick->joystick), name, sizeof(name));
#else
		const char* name = SDL_JoystickName(SDL_JoystickIndex(context->joystick->joystick));
		if (!name) {
			return;
		}
#endif
		char value[16];
		snprintf(value, sizeof(value), "%i", context->rotation.axisX);
		mInputSetCustomValue(config, "gba", SDL_BINDING_BUTTON, "tiltAxisX", value, name);
		snprintf(value, sizeof(value), "%i", context->rotation.axisY);
		mInputSetCustomValue(config, "gba", SDL_BINDING_BUTTON, "tiltAxisY", value, name);
		snprintf(value, sizeof(value), "%i", context->rotation.gyroX);
		mInputSetCustomValue(config, "gba", SDL_BINDING_BUTTON, "gyroAxisX", value, name);
		snprintf(value, sizeof(value), "%i", context->rotation.gyroY);
		mInputSetCustomValue(config, "gba", SDL_BINDING_BUTTON, "gyroAxisY", value, name);
		snprintf(value, sizeof(value), "%g", context->rotation.gyroSensitivity);
		mInputSetCustomValue(config, "gba", SDL_BINDING_BUTTON, "gyroSensitivity", value, name);
	}
}

void mSDLPlayerChangeJoystick(struct mSDLEvents* events, struct mSDLPlayer* player, size_t index) {
	if (player->playerId >= MAX_PLAYERS || index >= SDL_JoystickListSize(&events->joysticks)) {
		return;
	}
	player->joystick = SDL_JoystickListGetPointer(&events->joysticks, index);
}

void mSDLUpdateJoysticks(struct mSDLEvents* events, const struct Configuration* config) {
	// Pump SDL joystick events without eating the rest of the events
	SDL_JoystickUpdate();
#if SDL_VERSION_ATLEAST(2, 0, 0)
	SDL_Event event;
	while (SDL_PeepEvents(&event, 1, SDL_GETEVENT, SDL_JOYDEVICEADDED, SDL_JOYDEVICEREMOVED) > 0) {
		if (event.type == SDL_JOYDEVICEADDED) {
			SDL_Joystick* sdlJoystick = SDL_JoystickOpen(event.jdevice.which);
			if (!sdlJoystick) {
				continue;
			}
			ssize_t joysticks[MAX_PLAYERS];
			ssize_t i;
			// Pointers can get invalidated, so we'll need to refresh them
			for (i = 0; i < events->playersAttached && i < MAX_PLAYERS; ++i) {
				joysticks[i] = events->players[i]->joystick ? (ssize_t) SDL_JoystickListIndex(&events->joysticks, events->players[i]->joystick) : -1;
				events->players[i]->joystick = NULL;
			}
			struct SDL_JoystickCombo* joystick = SDL_JoystickListAppend(&events->joysticks);
			joystick->joystick = sdlJoystick;
			joystick->id = SDL_JoystickInstanceID(joystick->joystick);
			joystick->index = SDL_JoystickListSize(&events->joysticks) - 1;
#if SDL_VERSION_ATLEAST(2, 0, 0)
			joystick->haptic = SDL_HapticOpenFromJoystick(joystick->joystick);
#endif
			for (i = 0; i < events->playersAttached && i < MAX_PLAYERS; ++i) {
				if (joysticks[i] != -1) {
					events->players[i]->joystick = SDL_JoystickListGetPointer(&events->joysticks, joysticks[i]);
				}
			}

#if SDL_VERSION_ATLEAST(2, 0, 0)
			char joystickName[34] = {0};
			SDL_JoystickGetGUIDString(SDL_JoystickGetGUID(joystick->joystick), joystickName, sizeof(joystickName));
#else
			const char* joystickName = SDL_JoystickName(SDL_JoystickIndex(joystick->joystick));
			if (joystickName)
#endif
			{
				for (i = 0; (int) i < events->playersAttached; ++i) {
					if (events->players[i]->joystick) {
						continue;
					}
					if (events->preferredJoysticks[i] && strcmp(events->preferredJoysticks[i], joystickName) == 0) {
						events->players[i]->joystick = joystick;
						if (config) {
							mInputProfileLoad(events->players[i]->bindings, SDL_BINDING_BUTTON, config, joystickName);
						}
						return;
					}
				}
			}
			for (i = 0; (int) i < events->playersAttached; ++i) {
				if (events->players[i]->joystick) {
					continue;
				}
				events->players[i]->joystick = joystick;
				if (config
#if !SDL_VERSION_ATLEAST(2, 0, 0)
					&& joystickName
#endif
					) {
					mInputProfileLoad(events->players[i]->bindings, SDL_BINDING_BUTTON, config, joystickName);
				}
				break;
			}
		} else if (event.type == SDL_JOYDEVICEREMOVED) {
			SDL_JoystickID ids[MAX_PLAYERS] = { 0 };
			size_t i;
			for (i = 0; (int) i < events->playersAttached; ++i) {
				if (events->players[i]->joystick) {
					ids[i] = events->players[i]->joystick->id;
					events->players[i]->joystick = 0;
				} else {
					ids[i] = -1;
				}
			}
			for (i = 0; i < SDL_JoystickListSize(&events->joysticks);) {
				struct SDL_JoystickCombo* joystick = SDL_JoystickListGetPointer(&events->joysticks, i);
				if (joystick->id == event.jdevice.which) {
					SDL_JoystickListShift(&events->joysticks, i, 1);
					continue;
				}
				SDL_JoystickListGetPointer(&events->joysticks, i)->index = i;
				int p;
				for (p = 0; p < events->playersAttached; ++p) {
					if (joystick->id == ids[p]) {
						events->players[p]->joystick = SDL_JoystickListGetPointer(&events->joysticks, i);
					}
				}
				++i;
			}
		}
	}
#endif
}

static void _pauseAfterFrame(struct mCoreThread* context) {
	context->frameCallback = 0;
	mCoreThreadPauseFromThread(context);
}
//是否使用同步
void setSync(struct mCoreThread* context,bool sync,bool m_audioSync,bool m_videoSync) {
	if (sync) {
		context->impl->sync.audioWait = m_audioSync;
		context->impl->sync.videoFrameWait = m_videoSync;
	} else {
		context->impl->sync.audioWait = false;
		context->impl->sync.videoFrameWait = false;
	}
}
//按钮按下操作
void  onKeyDown(struct mCoreThread* context,int key){
	//mCoreThreadInterrupt(context);
	context->core->addKeys(context->core, 1 << key);
	//mCoreThreadContinue(context);
	return;
}
//按钮弹起操作
void onKeyUp(struct mCoreThread* context,int key){
	//mCoreThreadInterrupt(context);
	context->core->clearKeys(context->core, 1 << key);
	//mCoreThreadContinue(context);
	return;
}
//实现一些特殊操作
bool onKeySpecial(JNIEnv *_env,struct mCoreThread* context,int key,bool isDown) {
	if(key == SDLK_v){
		mCoreThreadInterrupt(context);
		if(isDown){
            ConfigurationSetIntValue(&context->core->config.defaultsTable, 0, "volume", 0x100);
		}else{
            ConfigurationSetIntValue(&context->core->config.defaultsTable, 0, "volume", 0);
		}
		context->core->reloadConfigOption(context->core,"volume",NULL);
		mCoreThreadContinue(context);
		return true;
	}
	if(key == SDLK_r){
		mCoreThreadInterrupt(context);
		mCoreThreadReset(context);
		mCoreThreadContinue(context);
		return true;
	}
	if (key == SDLK_TAB) {
		mCoreThreadInterrupt(context);
        bool m_videoSync = context->core->opts.videoSync;
        bool m_audioSync = context->core->opts.audioSync;
	    if(!isDown){
            setSync(context,!isDown,m_audioSync,m_videoSync);
	    }else{
            setSync(context,!isDown,m_audioSync,m_videoSync);
	    }
        context->core->reloadConfigOption(context->core, NULL, NULL);
		//context->impl->sync.audioWait = !isDown;
		mCoreThreadContinue(context);
		return true;
	}
	if (key == SDLK_BACKQUOTE) {
		mCoreThreadInterrupt(context);
		mCoreThreadSetRewinding(context, isDown);
		mCoreThreadContinue(context);
		return true;
	}
	if (key == SDLK_F12) {
		mCoreThreadInterrupt(context);
		if(!isDown){
			//注意screenshotPath是指针必须删除
			char*screenshotPath =  mCoreTakeScreenshot(context->core);
			//所有数据准备完成回调java,通知加载保存图片
			if(screenshotPath!=NULL){
				//char buffer[2048];
				jclass sdlActivity = (*_env)->FindClass(_env,"org/libsdl/app/SDLActivity");
				jmethodID mid = (*_env)->GetStaticMethodID(_env,sdlActivity,"gameScreenCapture","(Ljava/lang/String;)V");
				if(mid != NULL){
					//回调
					jstring capPath = (*_env)->NewStringUTF(_env,screenshotPath);
					(*_env)->CallStaticVoidMethod(_env,sdlActivity,mid,capPath);
					(*_env)->DeleteLocalRef(_env,capPath);
				}
			}
			if(screenshotPath!=NULL){
				free(screenshotPath);
				screenshotPath = NULL;
			}
		}
		mCoreThreadContinue(context);
		return true;
	}
	return false;
}

void onSlotKey(struct mCoreThread* context,int key ,bool isSave){
	if(isSave){
		mCoreThreadInterrupt(context);
		mCoreSaveState(context->core, key + 1,SAVESTATE_SAVEDATA | SAVESTATE_SCREENSHOT | SAVESTATE_RTC);
		mCoreThreadContinue(context);
	}else{
		mCoreThreadInterrupt(context);
		mCoreLoadState(context->core, key + 1, SAVESTATE_SCREENSHOT | SAVESTATE_RTC);
		mCoreThreadContinue(context);
	}
}

static void _mSDLHandleKeypress(struct mCoreThread* context, struct mSDLPlayer* sdlContext, const struct SDL_KeyboardEvent* event) {
//	int key = -1;
//	if (!(event->keysym.mod & ~(KMOD_NUM | KMOD_CAPS))) {
//		key = mInputMapKey(sdlContext->bindings, SDL_BINDING_KEY, event->keysym.sym);
//	}
//	if (key != -1) {
//		mCoreThreadInterrupt(context);
//		if (event->type == SDL_KEYDOWN) {
//			context->core->addKeys(context->core, 1 << key);
//		} else {
//			context->core->clearKeys(context->core, 1 << key);
//		}
//		mCoreThreadContinue(context);
//		return;
//	}
//	if (event->keysym.sym == SDLK_TAB) {
//		context->impl->sync.audioWait = event->type != SDL_KEYDOWN;
//		return;
//	}
//	if (event->keysym.sym == SDLK_BACKQUOTE) {
//		mCoreThreadSetRewinding(context, event->type == SDL_KEYDOWN);
//	}
//	if (event->type == SDL_KEYDOWN) {
//		switch (event->keysym.sym) {
//#ifdef USE_DEBUGGERS
//		case SDLK_F11:
//			if (context->core->debugger) {
//				mDebuggerEnter(context->core->debugger, DEBUGGER_ENTER_MANUAL, NULL);
//			}
//			return;
//#endif
//#ifdef USE_PNG
//			case SDLK_F12:
//        {
//            char *screenshotPath = mCoreTakeScreenshot(context->core);
//            if(screenshotPath!=NULL){
//                free(screenshotPath);
//                screenshotPath = NULL;
//            }
//            return;
//        }
//#endif
//		case SDLK_BACKSLASH:
//			mCoreThreadPause(context);
//			context->frameCallback = _pauseAfterFrame;
//			mCoreThreadUnpause(context);
//			return;
//#ifdef BUILD_PANDORA
//		case SDLK_ESCAPE:
//			mCoreThreadEnd(context);
//			return;
//#endif
//		default:
//			if ((event->keysym.mod & GUI_MOD) && (event->keysym.mod & GUI_MOD) == event->keysym.mod) {
//				switch (event->keysym.sym) {
//#if SDL_VERSION_ATLEAST(2, 0, 0)
//				case SDLK_f:
//					SDL_SetWindowFullscreen(sdlContext->window, sdlContext->fullscreen ? 0 : SDL_WINDOW_FULLSCREEN_DESKTOP);
//					sdlContext->fullscreen = !sdlContext->fullscreen;
//					sdlContext->windowUpdated = 1;
//					break;
//#endif
//				case SDLK_p:
//					mCoreThreadTogglePause(context);
//					break;
//				case SDLK_n:
//					mCoreThreadPause(context);
//					context->frameCallback = _pauseAfterFrame;
//					mCoreThreadUnpause(context);
//					break;
//				case SDLK_r:
//					mCoreThreadReset(context);
//					break;
//				default:
//					break;
//				}
//			}
//			if (event->keysym.mod & KMOD_SHIFT) {
//				switch (event->keysym.sym) {
//				case SDLK_F1:
//				case SDLK_F2:
//				case SDLK_F3:
//				case SDLK_F4:
//				case SDLK_F5:
//				case SDLK_F6:
//				case SDLK_F7:
//				case SDLK_F8:
//				case SDLK_F9:
//					mCoreThreadInterrupt(context);
//					mCoreSaveState(context->core, event->keysym.sym - SDLK_F1 + 1, SAVESTATE_SAVEDATA | SAVESTATE_SCREENSHOT | SAVESTATE_RTC);
//					mCoreThreadContinue(context);
//					break;
//				default:
//					break;
//				}
//			} else {
//				switch (event->keysym.sym) {
//				case SDLK_F1:
//				case SDLK_F2:
//				case SDLK_F3:
//				case SDLK_F4:
//				case SDLK_F5:
//				case SDLK_F6:
//				case SDLK_F7:
//				case SDLK_F8:
//				case SDLK_F9:
//					mCoreThreadInterrupt(context);
//					mCoreLoadState(context->core, event->keysym.sym - SDLK_F1 + 1, SAVESTATE_SCREENSHOT | SAVESTATE_RTC);
//					mCoreThreadContinue(context);
//					break;
//				default:
//					break;
//				}
//			}
//			return;
//		}
//	}
}

static void _mSDLHandleJoyButton(struct mCoreThread* context, struct mSDLPlayer* sdlContext, const struct SDL_JoyButtonEvent* event) {
//	int key = 0;
//	key = mInputMapKey(sdlContext->bindings, SDL_BINDING_BUTTON, event->button);
//	if (key == -1) {
//		return;
//	}
//
//	mCoreThreadInterrupt(context);
//	if (event->type == SDL_JOYBUTTONDOWN) {
//		context->core->addKeys(context->core, 1 << key);
//	} else {
//		context->core->clearKeys(context->core, 1 << key);
//	}
//	mCoreThreadContinue(context);
}

static void _mSDLHandleJoyHat(struct mCoreThread* context, struct mSDLPlayer* sdlContext, const struct SDL_JoyHatEvent* event) {
//	int allKeys = mInputMapHat(sdlContext->bindings, SDL_BINDING_BUTTON, event->hat, -1);
//	if (allKeys == 0) {
//		return;
//	}
//
//	int keys = mInputMapHat(sdlContext->bindings, SDL_BINDING_BUTTON, event->hat, event->value);
//
//	mCoreThreadInterrupt(context);
//	context->core->clearKeys(context->core, allKeys ^ keys);
//	context->core->addKeys(context->core, keys);
//	mCoreThreadContinue(context);
}

static void _mSDLHandleJoyAxis(struct mCoreThread* context, struct mSDLPlayer* sdlContext, const struct SDL_JoyAxisEvent* event) {
//	int clearKeys = ~mInputClearAxis(sdlContext->bindings, SDL_BINDING_BUTTON, event->axis, -1);
//	int newKeys = 0;
//	int key = mInputMapAxis(sdlContext->bindings, SDL_BINDING_BUTTON, event->axis, event->value);
//	if (key != -1) {
//		newKeys |= 1 << key;
//	}
//	clearKeys &= ~newKeys;
//	mCoreThreadInterrupt(context);
//	context->core->clearKeys(context->core, clearKeys);
//	context->core->addKeys(context->core, newKeys);
//	mCoreThreadContinue(context);

}

#if SDL_VERSION_ATLEAST(2, 0, 0)
static void _mSDLHandleWindowEvent(struct mSDLPlayer* sdlContext, const struct SDL_WindowEvent* event) {
	switch (event->event) {
	case SDL_WINDOWEVENT_SIZE_CHANGED:
		sdlContext->windowUpdated = 1;
		break;
	}
}
#endif

void mSDLHandleEvent(struct mCoreThread* context, struct mSDLPlayer* sdlContext, const union SDL_Event* event) {
//	switch (event->type) {
//	case SDL_QUIT:
//		mCoreThreadEnd(context);
//		break;
//#if SDL_VERSION_ATLEAST(2, 0, 0)
//	case SDL_WINDOWEVENT:
//		_mSDLHandleWindowEvent(sdlContext, &event->window);
//		break;
//#else
//	case SDL_VIDEORESIZE:
//		sdlContext->newWidth = event->resize.w;
//		sdlContext->newHeight = event->resize.h;
//		sdlContext->windowUpdated = 1;
//		break;
//#endif
//	case SDL_KEYDOWN:
//	case SDL_KEYUP:
//		_mSDLHandleKeypress(context, sdlContext, &event->key);
//		break;
//	case SDL_JOYBUTTONDOWN:
//	case SDL_JOYBUTTONUP:
//		_mSDLHandleJoyButton(context, sdlContext, &event->jbutton);
//		break;
//	case SDL_JOYHATMOTION:
//		_mSDLHandleJoyHat(context, sdlContext, &event->jhat);
//		break;
//	case SDL_JOYAXISMOTION:
//		_mSDLHandleJoyAxis(context, sdlContext, &event->jaxis);
//		break;
//	}
}

#if SDL_VERSION_ATLEAST(2, 0, 0)
static void _mSDLSetRumble(struct mRumble* rumble, int enable) {
	struct mSDLRumble* sdlRumble = (struct mSDLRumble*) rumble;
	if (!sdlRumble->p->joystick || !sdlRumble->p->joystick->haptic || !SDL_HapticRumbleSupported(sdlRumble->p->joystick->haptic)) {
		return;
	}
	int8_t originalLevel = sdlRumble->level;
	sdlRumble->level += enable;
	if (CircleBufferSize(&sdlRumble->history) == RUMBLE_PWM) {
		int8_t oldLevel;
		CircleBufferRead8(&sdlRumble->history, &oldLevel);
		sdlRumble->level -= oldLevel;
	}
	CircleBufferWrite8(&sdlRumble->history, enable);
	if (sdlRumble->level == originalLevel) {
		return;
	}
	float activeLevel = ceil(RUMBLE_STEPS * sdlRumble->level / (float) RUMBLE_PWM) / RUMBLE_STEPS;
	if (fabsf(sdlRumble->activeLevel - activeLevel) < 0.75 / RUMBLE_STEPS) {
		return;
	}
	sdlRumble->activeLevel = activeLevel;
	if (sdlRumble->activeLevel > 0.5 / RUMBLE_STEPS) {
		SDL_HapticRumbleStop(sdlRumble->p->joystick->haptic);
		SDL_HapticRumblePlay(sdlRumble->p->joystick->haptic, activeLevel, 500);
	} else {
		SDL_HapticRumbleStop(sdlRumble->p->joystick->haptic);
	}
}
#endif

static int32_t _readTilt(struct mSDLPlayer* player, int axis) {
	if (!player->joystick) {
		return 0;
	}
	return SDL_JoystickGetAxis(player->joystick->joystick, axis) * 0x3800;
}

static int32_t _mSDLReadTiltX(struct mRotationSource* source) {
	struct mSDLRotation* rotation = (struct mSDLRotation*) source;
	return _readTilt(rotation->p, rotation->axisX);
}

static int32_t _mSDLReadTiltY(struct mRotationSource* source) {
	struct mSDLRotation* rotation = (struct mSDLRotation*) source;
	return _readTilt(rotation->p, rotation->axisY);
}

static int32_t _mSDLReadGyroZ(struct mRotationSource* source) {
	struct mSDLRotation* rotation = (struct mSDLRotation*) source;
	float z = rotation->zDelta;
	return z * rotation->gyroSensitivity;
}

static void _mSDLRotationSample(struct mRotationSource* source) {
	struct mSDLRotation* rotation = (struct mSDLRotation*) source;
	SDL_JoystickUpdate();
	if (!rotation->p->joystick) {
		return;
	}

	int x = SDL_JoystickGetAxis(rotation->p->joystick->joystick, rotation->gyroX);
	int y = SDL_JoystickGetAxis(rotation->p->joystick->joystick, rotation->gyroY);
	union {
		float f;
		int32_t i;
	} theta = { .f = atan2f(y, x) - atan2f(rotation->oldY, rotation->oldX) };
	if (isnan(theta.f)) {
		theta.f = 0.0f;
	} else if (theta.f > M_PI) {
		theta.f -= 2.0f * M_PI;
	} else if (theta.f < -M_PI) {
		theta.f += 2.0f * M_PI;
	}
	rotation->oldX = x;
	rotation->oldY = y;

	float oldZ = 0;
	if (CircleBufferSize(&rotation->zHistory) == GYRO_STEPS * sizeof(float)) {
		CircleBufferRead32(&rotation->zHistory, (int32_t*) &oldZ);
	}
	CircleBufferWrite32(&rotation->zHistory, theta.i);
	rotation->zDelta += theta.f - oldZ;
}

#if SDL_VERSION_ATLEAST(2, 0, 0)
void mSDLSuspendScreensaver(struct mSDLEvents* events) {
	if (events->screensaverSuspendDepth == 0 && events->screensaverSuspendable) {
		SDL_DisableScreenSaver();
	}
	++events->screensaverSuspendDepth;
}

void mSDLResumeScreensaver(struct mSDLEvents* events) {
	--events->screensaverSuspendDepth;
	if (events->screensaverSuspendDepth == 0 && events->screensaverSuspendable) {
		SDL_EnableScreenSaver();
	}
}

void mSDLSetScreensaverSuspendable(struct mSDLEvents* events, bool suspendable) {
	bool wasSuspendable = events->screensaverSuspendable;
	events->screensaverSuspendable = suspendable;
	if (events->screensaverSuspendDepth > 0) {
		if (suspendable && !wasSuspendable) {
			SDL_DisableScreenSaver();
		} else if (!suspendable && wasSuspendable) {
			SDL_EnableScreenSaver();
		}
	} else {
		SDL_EnableScreenSaver();
	}
}
#endif
