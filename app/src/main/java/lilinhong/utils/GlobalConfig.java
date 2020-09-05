package lilinhong.utils;


public class GlobalConfig {
    public static String GBA_SUFFIX = "gba";
    public static String GBC_SUFFIX = "gbc";
    public static String GB_SUFFIX = "gb";
    public static String SGB_SUFFIX ="sgb";

    //是否搜索.开头目录文件（这个文件夹一般是系统文件夹，所以不搜索）
    public static boolean SEARCH_DOT = false;
    //判断是不是第一次加载游戏作弊（重要）
    public static String FIRST_RUN_GAME_CHEAT = "";
    //shi是否显示虚拟按键
    public static boolean VIRTUAL_BUTTON_CONTROL = true;
    //封面数据由于线程异步直接通过sharexxx保存
    public static String GAME_COVER = "";
}
