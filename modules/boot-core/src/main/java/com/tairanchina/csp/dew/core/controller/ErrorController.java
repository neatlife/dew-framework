package com.tairanchina.csp.dew.core.controller;

import com.ecfront.dew.common.$;
import com.ecfront.dew.common.Resp;
import com.fasterxml.jackson.databind.JsonNode;
import com.tairanchina.csp.dew.core.Dew;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("${error.path:/error}")
@ConditionalOnProperty(prefix = "dew.basic.format", name = "useUnityError", havingValue = "true")
public class ErrorController extends AbstractErrorController {

    protected static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    private static Pattern MESSAGE_CHECK = Pattern.compile("^\\{\"code\":\"\\w*\",\"message\":\".*\"}$");

    @Value("${error.path:/error}")
    private String errorPath;

    public ErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @Override
    public String getErrorPath() {
        return errorPath;
    }


    @RequestMapping()
    @ResponseBody
    public Object errorByHttpState(HttpServletRequest request) {
        Map<String, Object> error = getErrorAttributes(request, false);
        String path = (String) error.getOrDefault("path", Dew.context().getRequestUri());
        int code = (int) error.getOrDefault("status", -1);
        String err = (String) error.getOrDefault("error", "");
        String message = error.getOrDefault("message", "") + "";
        logger.error("Request [{}] from {} , error {} : {}", path, Dew.context().getSourceIP(), code, message);
        if (!Dew.dewConfig.getBasic().getFormat().isReuseHttpState()) {
            return Resp.customFail(code + "", "[" + err + "]" + message);
        } else {
            JsonNode jsonNode;
            if (MESSAGE_CHECK.matcher(message).matches()) {
                jsonNode = $.json.createObjectNode()
                        .set("error", $.json.toJson(message));
            } else {
                jsonNode = $.json.createObjectNode()
                        .set("error", $.json.createObjectNode()
                                .put("code", code)
                                .put("message", message));
            }
            return ResponseEntity.status(HttpStatus.valueOf(code)).body(jsonNode);
        }
    }


}
