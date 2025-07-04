/* Copyright (c) 2013-2015 Jeffrey Pfau
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
#include "main.h"

#include <mgba/core/core.h>
#include <mgba/core/thread.h>
#include <mgba/core/version.h>
#include <mgba-util/arm-algo.h>
#include <include/mgba/gba/interface.h>

//默认全屏
int fullScreen = 1;

static bool mSDLSWInit(struct mSDLRenderer* renderer);
static void mSDLSWRunloop(struct mSDLRenderer* renderer, void* user);
static void mSDLSWDeinit(struct mSDLRenderer* renderer);

void mSDLSWCreate(struct mSDLRenderer* renderer) {
	renderer->init = mSDLSWInit;
	renderer->deinit = mSDLSWDeinit;
	renderer->runloop = mSDLSWRunloop;
}

bool mSDLSWInit(struct mSDLRenderer* renderer) {
	unsigned width, height;
	width = 0;
	height = 0;
	renderer->core->desiredVideoDimensions(renderer->core, &width, &height);
	int flags = fullScreen ? SDL_WINDOW_FULLSCREEN_DESKTOP : 0;
	renderer->window = SDL_CreateWindow("mGBA", SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, renderer->viewportWidth, renderer->viewportHeight, flags);
	SDL_GetWindowSize(renderer->window, &renderer->viewportWidth, &renderer->viewportHeight);
	renderer->player.window = renderer->window;
	renderer->sdlRenderer = SDL_CreateRenderer(renderer->window, -1, SDL_RENDERER_ACCELERATED | SDL_RENDERER_PRESENTVSYNC);
#ifdef COLOR_16_BIT
#ifdef COLOR_5_6_5
	renderer->sdlTex = SDL_CreateTexture(renderer->sdlRenderer, SDL_PIXELFORMAT_RGB565, SDL_TEXTUREACCESS_STREAMING, width, height);
#else
	renderer->sdlTex = SDL_CreateTexture(renderer->sdlRenderer, SDL_PIXELFORMAT_ABGR1555, SDL_TEXTUREACCESS_STREAMING, width, height);
#endif
#else
	renderer->sdlTex = SDL_CreateTexture(renderer->sdlRenderer, SDL_PIXELFORMAT_ABGR8888, SDL_TEXTUREACCESS_STREAMING, width, height);
#endif

	int stride;
	SDL_LockTexture(renderer->sdlTex, 0, (void**) &renderer->outputBuffer, &stride);
	renderer->core->setVideoBuffer(renderer->core, renderer->outputBuffer, stride / BYTES_PER_PIXEL);

	return true;
}

void mSDLSWRunloop(struct mSDLRenderer* renderer, void* user) {
	struct mCoreThread* context = user;
	SDL_Event event;

    float width = renderer->width;
    float height = renderer->height;
    if(renderer->width!=GBA_VIDEO_HORIZONTAL_PIXELS){
		float SCREENT_X = 256.0f/width;
		float SCREENT_Y = 224.0f/height;

		int screent_w = (renderer->viewportWidth * SCREENT_X);
		int screent_h = (renderer->viewportHeight * SCREENT_Y);

		if(screent_w > SCREENT_RECT.w){
			SCREENT_RECT.w = screent_w;
		}

		if(screent_h > SCREENT_RECT.h){
			SCREENT_RECT.h = screent_h;
		}
    }else{
		SCREENT_RECT.w = renderer->viewportWidth;
		SCREENT_RECT.h = renderer->viewportHeight;
    }

	//所有数据准备完成回调java,通知加载默认保存记录
	{
		if(renderer->_env!=NULL){
			jclass sdlActivity = (*renderer->_env)->FindClass(renderer->_env,"org/libsdl/app/SDLActivity");
			jmethodID mid = (*renderer->_env)->GetStaticMethodID(renderer->_env,sdlActivity,"gameLoadFinish","()V");
			if(mid != NULL){
				//回调
				(*renderer->_env)->CallStaticVoidMethod(renderer->_env,sdlActivity,mid);
				(*renderer->_env)->DeleteLocalRef(renderer->_env,sdlActivity);
			}
		}
	}

    //SCREENT_RECT = {0, 0, vwidth * SCREENT_X, vheight * SCREENT_Y};
	while (mCoreThreadIsActive(context)) {
		while (SDL_PollEvent(&event)) {
			mSDLHandleEvent(context, &renderer->player, &event);
		}

		if (mCoreSyncWaitFrameStart(&context->impl->sync)) {
			SDL_UnlockTexture(renderer->sdlTex);
			SDL_RenderCopy(renderer->sdlRenderer, renderer->sdlTex, 0, &SCREENT_RECT);
			SDL_RenderPresent(renderer->sdlRenderer);
			int stride;
			SDL_LockTexture(renderer->sdlTex, 0, (void**) &renderer->outputBuffer, &stride);
			renderer->core->setVideoBuffer(renderer->core, renderer->outputBuffer, stride / BYTES_PER_PIXEL);
		}
		mCoreSyncWaitFrameEnd(&context->impl->sync);
	}
}

void mSDLSWDeinit(struct mSDLRenderer* renderer) {
	if (renderer->ratio > 1) {
		free(renderer->outputBuffer);
	}
}
