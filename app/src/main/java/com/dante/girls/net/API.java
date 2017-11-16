package com.dante.girls.net;

/**
 * 图片源：
 * https://www.dbmeinv.com/
 *
 * 动漫：
 * http://www.apic.in/
 *
 * 干货集中营：
 * 每日分享妹子图 和 技术干货，还有供大家中午休息的休闲视频
 * http://gank.io/
 *
 * 关于这部分，Android 编程权威指南中有很好的示例，
 * 根据该项目提供的API，我可以继续完成我的APP
 *
 * Net request APIs and types
 */
public class API {
    /** 知乎 **/
    public static final String SPLASH = "http://news-at.zhihu.com/api/";

    /** 这里主要作用 获取 APP info，对比判断是否有必要更新 **/
    /** GitHub **/
    //public static final String GITHUB_RAW = "https://raw.githubusercontent.com/DanteAndroid/Beauty/master/";
    public static final String GITHUB_RAW = "https://github.com/p1198528948/apk-release/blob/master/";

    //public static final String DOWNLOAD_BASE = "https://github.com/DanteAndroid/Beauty/releases/download/";
    public static final String DOWNLOAD_BASE = "https://github.com/p1198528948/download/tree/master/download/";

    //Gank
    public static final String TYPE_GANK = "0";
    //豆瓣
    public static final String TYPE_DB_BREAST = "2";
    public static final String TYPE_DB_BUTT = "6";
    public static final String TYPE_DB_SILK = "7";
    public static final String TYPE_DB_LEG = "3";
    public static final String TYPE_DB_RANK = "5";

    //A区
    public static final String TYPE_A_ANIME = "anime";
    public static final String TYPE_A_UNIFORM = "zhifu";
    public static final String TYPE_A_HENTAI = "hentai";
    //    public static final String TYPE_A_YSJ = "yusanjia";
    public static final String TYPE_A_ZATU = "zatuji";
    public static final String TYPE_A_FULI = "fuli";

    //Base API
    public static String GANK = "http://gank.io/api/";
    public static String DB_BASE = "http://www.dbmeinv.com/dbgroup/";
    public static String A_BASE = "http://www.apic.in/";

    //H API
    public static String H_BASE = "http://bww.yakexi.biz/pw/";
    public static String H_MAIN = H_BASE + "thread.php?fid=";

}
