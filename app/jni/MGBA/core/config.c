/* Copyright (c) 2013-2016 Jeffrey Pfau
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
#include <mgba/core/config.h>

#include <mgba/core/version.h>
#include <mgba-util/formatting.h>
#include <mgba-util/string.h>
#include <mgba-util/vfs.h>

#include <sys/stat.h>

#ifdef _WIN32
#include <windows.h>
#include <shlwapi.h>
#include <shlobj.h>
#include <strsafe.h>
#endif

#ifdef PSP2
#include <psp2/io/stat.h>
#endif

#ifdef _3DS
#include <mgba-util/platform/3ds/3ds-vfs.h>
#endif

#define SECTION_NAME_MAX 128

struct mCoreConfigEnumerateData {
	void (*handler)(const char* key, const char* value, enum mCoreConfigLevel type, void* user);
	const char* prefix;
	void* user;
	enum mCoreConfigLevel level;
};

static const char* _lookupValue(const struct mCoreConfig* config, const char* key) {
	const char* value;
	if (config->port) {
		value = ConfigurationGetValue(&config->overridesTable, config->port, key);
		if (value) {
			return value;
		}
	}
	value = ConfigurationGetValue(&config->overridesTable, 0, key);
	if (value) {
		return value;
	}
	if (config->port) {
		value = ConfigurationGetValue(&config->configTable, config->port, key);
		if (value) {
			return value;
		}
	}
	value = ConfigurationGetValue(&config->configTable, 0, key);
	if (value) {
		return value;
	}
	if (config->port) {
		value = ConfigurationGetValue(&config->defaultsTable, config->port, key);
		if (value) {
			return value;
		}
	}
	return ConfigurationGetValue(&config->defaultsTable, 0, key);
}

static bool _lookupCharValue(const struct mCoreConfig* config, const char* key, char** out) {
	const char* value = _lookupValue(config, key);
	if (!value) {
		return false;
	}
	if (*out) {
		free(*out);
	}
	*out = strdup(value);
	return true;
}

static bool _lookupIntValue(const struct mCoreConfig* config, const char* key, int* out) {
	const char* charValue = _lookupValue(config, key);
	if (!charValue) {
		return false;
	}
	char* end;
	long value = strtol(charValue, &end, 10);
	if (end == &charValue[1] && *end == 'x') {
		value = strtol(charValue, &end, 16);
	}
	if (*end) {
		return false;
	}
	*out = value;
	return true;
}

static bool _lookupUIntValue(const struct mCoreConfig* config, const char* key, unsigned* out) {
	const char* charValue = _lookupValue(config, key);
	if (!charValue) {
		return false;
	}
	char* end;
	unsigned long value = strtoul(charValue, &end, 10);
	if (*end) {
		return false;
	}
	*out = value;
	return true;
}

static bool _lookupFloatValue(const struct mCoreConfig* config, const char* key, float* out) {
	const char* charValue = _lookupValue(config, key);
	if (!charValue) {
		return false;
	}
	char* end;
	float value = strtof_u(charValue, &end);
	if (*end) {
		return false;
	}
	*out = value;
	return true;
}

void mCoreConfigInit(struct mCoreConfig* config, const char* port) {
	ConfigurationInit(&config->configTable);
	ConfigurationInit(&config->defaultsTable);
	ConfigurationInit(&config->overridesTable);
	if (port) {
		config->port = malloc(strlen("ports.") + strlen(port) + 1);
		snprintf(config->port, strlen("ports.") + strlen(port) + 1, "ports.%s", port);
	} else {
		config->port = 0;
	}
}

void mCoreConfigDeinit(struct mCoreConfig* config) {
	ConfigurationDeinit(&config->configTable);
	ConfigurationDeinit(&config->defaultsTable);
	ConfigurationDeinit(&config->overridesTable);
	free(config->port);
}

#if !defined(MINIMAL_CORE) || MINIMAL_CORE < 2
bool mCoreConfigLoad(struct mCoreConfig* config) {
	char path[PATH_MAX];
	mCoreConfigDirectory(path, PATH_MAX);
	strncat(path, PATH_SEP "config.ini", PATH_MAX - strlen(path));
	return mCoreConfigLoadPath(config, path);
}

bool mCoreConfigSave(const struct mCoreConfig* config) {
	char path[PATH_MAX];
	mCoreConfigDirectory(path, PATH_MAX);
	strncat(path, PATH_SEP "config.ini", PATH_MAX - strlen(path));
	return mCoreConfigSavePath(config, path);
}

bool mCoreConfigLoadPath(struct mCoreConfig* config, const char* path) {
	return ConfigurationRead(&config->configTable, path);
}

bool mCoreConfigSavePath(const struct mCoreConfig* config, const char* path) {
	return ConfigurationWrite(&config->configTable, path);
}

void mCoreConfigMakePortable(const struct mCoreConfig* config) {
	struct VFile* portable = 0;
#ifdef _WIN32
	char out[MAX_PATH];
	wchar_t wpath[MAX_PATH];
	wchar_t wprojectName[MAX_PATH];
	MultiByteToWideChar(CP_UTF8, 0, projectName, -1, wprojectName, MAX_PATH);
	HMODULE hModule = GetModuleHandleW(NULL);
	GetModuleFileNameW(hModule, wpath, MAX_PATH);
	PathRemoveFileSpecW(wpath);
	WideCharToMultiByte(CP_UTF8, 0, wpath, -1, out, MAX_PATH, 0, 0);
	StringCchCatA(out, MAX_PATH, "\\portable.ini");
	portable = VFileOpen(out, O_WRONLY | O_CREAT);
#elif defined(PSP2) || defined(_3DS) || defined(__SWITCH__) || defined(GEKKO)
	// Already portable
#else
	char out[PATH_MAX];
	getcwd(out, PATH_MAX);
	strncat(out, PATH_SEP "portable.ini", PATH_MAX - strlen(out));
	portable = VFileOpen(out, O_WRONLY | O_CREAT);
#endif
	if (portable) {
		portable->close(portable);
		mCoreConfigSave(config);
	}
}

void mCoreConfigDirectory(char* out, size_t outLength) {
	struct VFile* portable;
#ifdef _WIN32
	wchar_t wpath[MAX_PATH];
	wchar_t wprojectName[MAX_PATH];
	MultiByteToWideChar(CP_UTF8, 0, projectName, -1, wprojectName, MAX_PATH);
	HMODULE hModule = GetModuleHandleW(NULL);
	GetModuleFileNameW(hModule, wpath, MAX_PATH);
	PathRemoveFileSpecW(wpath);
	WideCharToMultiByte(CP_UTF8, 0, wpath, -1, out, outLength, 0, 0);
	StringCchCatA(out, outLength, "\\portable.ini");
	portable = VFileOpen(out, O_RDONLY);
	if (portable) {
		portable->close(portable);
	} else {
		wchar_t* home;
		SHGetKnownFolderPath(&FOLDERID_RoamingAppData, 0, NULL, &home);
		StringCchPrintfW(wpath, MAX_PATH, L"%ws\\%ws", home, wprojectName);
		CoTaskMemFree(home);
		CreateDirectoryW(wpath, NULL);
	}
	WideCharToMultiByte(CP_UTF8, 0, wpath, -1, out, outLength, 0, 0);
#elif defined(PSP2)
	UNUSED(portable);
	snprintf(out, outLength, "ux0:data/%s", projectName);
	sceIoMkdir(out, 0777);
#elif defined(GEKKO) || defined(__SWITCH__)
	UNUSED(portable);
	snprintf(out, outLength, "/%s", projectName);
	mkdir(out, 0777);
#elif defined(_3DS)
	UNUSED(portable);
	snprintf(out, outLength, "/%s", projectName);
	FSUSER_CreateDirectory(sdmcArchive, fsMakePath(PATH_ASCII, out), 0);
#else
	getcwd(out, outLength);
	strncat(out, PATH_SEP "portable.ini", outLength - strlen(out));
	portable = VFileOpen(out, O_RDONLY);
	if (portable) {
		getcwd(out, outLength);
		portable->close(portable);
		return;
	}

	char* xdgConfigHome = getenv("XDG_CONFIG_HOME");
	if (xdgConfigHome && xdgConfigHome[0] == '/') {
		snprintf(out, outLength, "%s/%s", xdgConfigHome, "mGBA");
		mkdir(out, 0755);
		return;
	}
	char* home = getenv("HOME");
	snprintf(out, outLength, "%s/.config", home);
	mkdir(out, 0755);
	snprintf(out, outLength, "%s/.config/%s", home, "mGBA");
	mkdir(out, 0755);
#endif
}
#endif

const char* mCoreConfigGetValue(const struct mCoreConfig* config, const char* key) {
	return _lookupValue(config, key);
}

bool mCoreConfigGetIntValue(const struct mCoreConfig* config, const char* key, int* value) {
	return _lookupIntValue(config, key, value);
}

bool mCoreConfigGetUIntValue(const struct mCoreConfig* config, const char* key, unsigned* value) {
	return _lookupUIntValue(config, key, value);
}

bool mCoreConfigGetFloatValue(const struct mCoreConfig* config, const char* key, float* value) {
	return _lookupFloatValue(config, key, value);
}

void mCoreConfigSetValue(struct mCoreConfig* config, const char* key, const char* value) {
	ConfigurationSetValue(&config->configTable, config->port, key, value);
}

void mCoreConfigSetIntValue(struct mCoreConfig* config, const char* key, int value) {
	ConfigurationSetIntValue(&config->configTable, config->port, key, value);
}

void mCoreConfigSetUIntValue(struct mCoreConfig* config, const char* key, unsigned value) {
	ConfigurationSetUIntValue(&config->configTable, config->port, key, value);
}

void mCoreConfigSetFloatValue(struct mCoreConfig* config, const char* key, float value) {
	ConfigurationSetFloatValue(&config->configTable, config->port, key, value);
}

void mCoreConfigSetDefaultValue(struct mCoreConfig* config, const char* key, const char* value) {
	ConfigurationSetValue(&config->defaultsTable, config->port, key, value);
}

void mCoreConfigSetDefaultIntValue(struct mCoreConfig* config, const char* key, int value) {
	ConfigurationSetIntValue(&config->defaultsTable, config->port, key, value);
}

void mCoreConfigSetDefaultUIntValue(struct mCoreConfig* config, const char* key, unsigned value) {
	ConfigurationSetUIntValue(&config->defaultsTable, config->port, key, value);
}

void mCoreConfigSetDefaultFloatValue(struct mCoreConfig* config, const char* key, float value) {
	ConfigurationSetFloatValue(&config->defaultsTable, config->port, key, value);
}

void mCoreConfigSetOverrideValue(struct mCoreConfig* config, const char* key, const char* value) {
	ConfigurationSetValue(&config->overridesTable, config->port, key, value);
}

void mCoreConfigSetOverrideIntValue(struct mCoreConfig* config, const char* key, int value) {
	ConfigurationSetIntValue(&config->overridesTable, config->port, key, value);
}

void mCoreConfigSetOverrideUIntValue(struct mCoreConfig* config, const char* key, unsigned value) {
	ConfigurationSetUIntValue(&config->overridesTable, config->port, key, value);
}

void mCoreConfigSetOverrideFloatValue(struct mCoreConfig* config, const char* key, float value) {
	ConfigurationSetFloatValue(&config->overridesTable, config->port, key, value);
}

void mCoreConfigCopyValue(struct mCoreConfig* config, const struct mCoreConfig* src, const char* key) {
	const char* value = mCoreConfigGetValue(src, key);
	if (!value) {
		return;
	}
	mCoreConfigSetValue(config, key, value);
}

void mCoreConfigMap(const struct mCoreConfig* config, struct mCoreOptions* opts) {
	_lookupCharValue(config, "bios", &opts->bios);
	_lookupCharValue(config, "shader", &opts->shader);
	_lookupIntValue(config, "logLevel", &opts->logLevel);
	_lookupIntValue(config, "frameskip", &opts->frameskip);
	_lookupIntValue(config, "volume", &opts->volume);
	_lookupIntValue(config, "rewindBufferCapacity", &opts->rewindBufferCapacity);
	_lookupFloatValue(config, "fpsTarget", &opts->fpsTarget);
	unsigned audioBuffers;
	if (_lookupUIntValue(config, "audioBuffers", &audioBuffers)) {
		opts->audioBuffers = audioBuffers;
	}
	_lookupUIntValue(config, "sampleRate", &opts->sampleRate);

	int fakeBool;
	if (_lookupIntValue(config, "useBios", &fakeBool)) {
		opts->useBios = fakeBool;
	}
	if (_lookupIntValue(config, "audioSync", &fakeBool)) {
		opts->audioSync = fakeBool;
	}
	if (_lookupIntValue(config, "videoSync", &fakeBool)) {
		opts->videoSync = fakeBool;
	}
	if (_lookupIntValue(config, "lockAspectRatio", &fakeBool)) {
		opts->lockAspectRatio = fakeBool;
	}
	if (_lookupIntValue(config, "lockIntegerScaling", &fakeBool)) {
		opts->lockIntegerScaling = fakeBool;
	}
	if (_lookupIntValue(config, "interframeBlending", &fakeBool)) {
		opts->interframeBlending = fakeBool;
	}
	if (_lookupIntValue(config, "resampleVideo", &fakeBool)) {
		opts->resampleVideo = fakeBool;
	}
	if (_lookupIntValue(config, "suspendScreensaver", &fakeBool)) {
		opts->suspendScreensaver = fakeBool;
	}
	if (_lookupIntValue(config, "mute", &fakeBool)) {
		opts->mute = fakeBool;
	}
	if (_lookupIntValue(config, "skipBios", &fakeBool)) {
		opts->skipBios = fakeBool;
	}
	if (_lookupIntValue(config, "rewindEnable", &fakeBool)) {
		opts->rewindEnable = false;
	}

	_lookupIntValue(config, "fullscreen", &opts->fullscreen);
	_lookupIntValue(config, "width", &opts->width);
	_lookupIntValue(config, "height", &opts->height);

	_lookupCharValue(config, "savegamePath", &opts->savegamePath);
	_lookupCharValue(config, "savestatePath", &opts->savestatePath);
	_lookupCharValue(config, "screenshotPath", &opts->screenshotPath);
	_lookupCharValue(config, "patchPath", &opts->patchPath);
	_lookupCharValue(config, "cheatsPath", &opts->cheatsPath);
}

void mCoreConfigLoadDefaults(struct mCoreConfig* config, const struct mCoreOptions* opts) {
	ConfigurationSetValue(&config->defaultsTable, 0, "bios", opts->bios);
	ConfigurationSetValue(&config->defaultsTable, 0, "shader", opts->shader);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "skipBios", opts->skipBios);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "useBios", opts->useBios);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "logLevel", opts->logLevel);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "frameskip", opts->frameskip);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "rewindEnable", opts->rewindEnable);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "rewindBufferCapacity", opts->rewindBufferCapacity);
	ConfigurationSetFloatValue(&config->defaultsTable, 0, "fpsTarget", opts->fpsTarget);
	ConfigurationSetUIntValue(&config->defaultsTable, 0, "audioBuffers", opts->audioBuffers);
	ConfigurationSetUIntValue(&config->defaultsTable, 0, "sampleRate", opts->sampleRate);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "audioSync", opts->audioSync);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "videoSync", opts->videoSync);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "fullscreen", opts->fullscreen);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "width", opts->width);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "height", opts->height);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "volume", opts->volume);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "mute", opts->mute);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "lockAspectRatio", opts->lockAspectRatio);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "lockIntegerScaling", opts->lockIntegerScaling);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "resampleVideo", opts->resampleVideo);
	ConfigurationSetIntValue(&config->defaultsTable, 0, "suspendScreensaver", opts->suspendScreensaver);
}

static void _configEnum(const char* key, const char* value, void* user) {
	struct mCoreConfigEnumerateData* data = user;
	if (!data->prefix || startswith(key, data->prefix)) {
		data->handler(key, value, data->level, data->user);
	}
}

void mCoreConfigEnumerate(const struct mCoreConfig* config, const char* prefix, void (*handler)(const char* key, const char* value, enum mCoreConfigLevel type, void* user), void* user) {
	struct mCoreConfigEnumerateData handlerData = { handler, prefix, user, mCONFIG_LEVEL_DEFAULT };
	ConfigurationEnumerate(&config->defaultsTable, config->port, _configEnum, &handlerData);
	handlerData.level = mCONFIG_LEVEL_CUSTOM;
	ConfigurationEnumerate(&config->configTable, config->port, _configEnum, &handlerData);
	handlerData.level = mCONFIG_LEVEL_OVERRIDE;
	ConfigurationEnumerate(&config->overridesTable, config->port, _configEnum, &handlerData);
}

// These two are basically placeholders in case the internal layout changes, e.g. for loading separate files
struct Configuration* mCoreConfigGetInput(struct mCoreConfig* config) {
	return &config->configTable;
}

struct Configuration* mCoreConfigGetOverrides(struct mCoreConfig* config) {
	return &config->configTable;
}

const struct Configuration* mCoreConfigGetOverridesConst(const struct mCoreConfig* config) {
	return &config->configTable;
}

void mCoreConfigFreeOpts(struct mCoreOptions* opts) {
	free(opts->bios);
	free(opts->shader);
	free(opts->savegamePath);
	free(opts->savestatePath);
	free(opts->screenshotPath);
	free(opts->patchPath);
	free(opts->cheatsPath);
	opts->bios = 0;
	opts->shader = 0;
	opts->savegamePath = 0;
	opts->savestatePath = 0;
	opts->screenshotPath = 0;
	opts->patchPath = 0;
	opts->cheatsPath = 0;
}
