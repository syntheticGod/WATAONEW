/**
 *
 */
package me.wowtao.pottery;


/**
 * @author qiuch
 */
public class Constants {
    // 定义网络连接的时间
    public static final int CONNECT_TIMEOUT = 1000 * 10;
    public static final int READ_TIMEOUT = 1000 * 20;

    //url
    public static final String HOME_URL = "http://watao.jian-yin.com/index.php/";
//    public static final String HOME_URL = "http://watao-test.jian-yin.com/index.php/";

    public static final String BASE_URL = HOME_URL + "webservices/";
    public static final String GET_DECORATOR_URL = BASE_URL + "get_decorators_list";
    public static final String GET_BASE_PRICE_URL = BASE_URL + "";
    public static final String UPLOAD_ORDER = BASE_URL + "save_order";
    public static final String LOGIN_URL = BASE_URL + "save_user";
    public static final String CHANGE_ORDER_STATUS = BASE_URL + "update_order_flag";
    public static final String UPLOAD_IMAGE_URL = BASE_URL + "image_upload";
    public static final String GET_IMAGE_URL = HOME_URL + "pages/share?image=";
}