package lilinhong.utils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import org.libsdl.app.R;
import org.libsdl.app.SDLActivity;

import lilinhong.activity.MainActivity;
import lilinhong.model.GameRom;

public class AdmobHelper {
    private GameRom gameRom = null;
    private static MainActivity mActivity;
    private static String APP_ID = ""; //正式的
    private static String AD_UNIT_ID = ""; //正式的
    //谷歌广告
    private static InterstitialAd mInterstitialAd = null;
    private int adViewCount = 5;
    private int adChaye = 5;
    public AdmobHelper(MainActivity activity) {
        mActivity = activity;
        APP_ID = mActivity.getString(R.string.ap_ads_main);
        AD_UNIT_ID = mActivity.getString(R.string.ap_ads_chanye);
        MobileAds.initialize(activity, APP_ID);
        mInterstitialAd = new InterstitialAd(mActivity);
        mInterstitialAd.setAdUnitId(AD_UNIT_ID);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adChaye = 3;
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdClosed() {
                adChaye = 3;
                loadRewardedVideoAd();
                loadGame();
            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                adChaye -= 1;
                if(adChaye<=0){
                    return;
                }
                loadRewardedVideoAd();
                Log.e(AdmobHelper.class.getName(),"errorCode:"+errorCode);
            }
        });
        loadRewardedVideoAd();

    }

    //加载一个广告
    public void loadRewardedVideoAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }
    //显示插页广告
    public boolean showInterstitial(boolean isOnlyAd){

        if(isOnlyAd){
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                return true;
            } else {
                loadGame();
                return true;
            }
        }

        if(adViewCount<=0){
            adViewCount = 5;
        }
        adViewCount -= 1;
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            return true;
        } else {
            loadRewardedVideoAd();
            return false;
        }
    }

    private void loadGame() {
        Intent sdlActivityIntent = new Intent(mActivity, SDLActivity.class);
        Bundle bundle=new Bundle();
        bundle.putSerializable("GAME_ROM",gameRom);
        sdlActivityIntent.putExtras(bundle);
        mActivity.startActivity(sdlActivityIntent);
    }

    public void setGameDescription(GameRom gameRom){
        this.gameRom = gameRom;
    }
}
