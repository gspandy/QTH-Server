package com.nowbook.restful.security.jwt.extractor;

import com.nowbook.common.utils.JsonMapper;
import com.nowbook.web.interceptor.LoginInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by robin on 17/3/10.
 */

public class ResponseUtil {
    private final static Logger log = LoggerFactory.getLogger(LoginInterceptor.class);
    /**
     * 客户端返回JSON字符串
     *
     * @param response
     * @param object
     * @return
     */
    public static void renderString(HttpServletResponse response, Object object) throws IOException {
        renderString(response, JsonMapper.nonDefaultMapper().toJson(object), MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 客户端返回字符串
     *
     * @param response
     * @param string
     * @return
     */
    public static void renderString(HttpServletResponse response, String string, String type) throws IOException {
        try{
            //response.reset();
            response.setContentType(type);
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(string);

        } catch (Exception ex) {
            log.error("Handling of @Response resulted in Exception", ex);
        } finally {
                response.getWriter().close();


        }
       //response.getWriter().flush();

    }
    /**
     * 客户端返回JSON字符串
     *
     * @param response
     * @param error
     * @return
     */
    public static void renderErrorString(HttpServletResponse response, String error) throws IOException {
//        Response res=new Response();
//        NbResponse<Boolean> result = new NbResponse<Boolean>();
//        result.setResult(false);
//        result.setError(error);
//        res.setData(result);
//        res.setStatus(401);
//        renderString(response, JsonMapper.nonEmptyMapper().toJson(res), MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(401);
        renderString(response, error, MediaType.APPLICATION_JSON_VALUE);
    }
    /**
     * 客户端返回JSON字符串
     *
     * @param response
     * @param error
     * @return
     */
    public static void renderErrorString(HttpServletResponse response, String error,int status) throws IOException {

//        Response res=new Response();
//        NbResponse<Boolean> result = new NbResponse<Boolean>();
//        result.setResult(false);
//        result.setError(error);
//        res.setData(result);
        response.setStatus(status);
        renderString(response, error, MediaType.APPLICATION_JSON_VALUE);
    }
}
