package com.github.fishlikewater.httppierce.exception;


import com.github.fishlikewater.httppierce.api.CodeEnum;
import com.github.fishlikewater.httppierce.api.Result;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 统一处理异常
 */
@ConditionalOnProperty(prefix = "http.pierce", name = "boot-type", havingValue = "client")
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Result<Object> processMethod(MissingServletRequestParameterException ex){
        log.error("异常信息：", ex);
        return new Result<>(CodeEnum.PARAMETER_ERROR, "参数[" + ex.getParameterName() + "]不能为空");
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Result<Object> processMethod(HttpMessageNotReadableException ex){
        log.error("异常信息：", ex);
        return new Result<>(CodeEnum.PARAMETER_ERROR,"请求参数不合法");
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<Object> processMethod(MethodArgumentNotValidException ex){
        log.error("异常信息：", ex);
        return new Result<>(CodeEnum.PARAMETER_ERROR, ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }


    //参数校验
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result<Object> processMethod(ConstraintViolationException ex){
        log.error("异常信息：", ex);
        return new Result<>(CodeEnum.PARAMETER_ERROR, ex.getMessage());
    }

    //参数校验
    @ExceptionHandler(value = BindException.class)
    public Result<Object> processMethod(BindException ex){
        log.error("异常信息：", ex);
        return new Result<>(CodeEnum.PARAMETER_ERROR, ex.getMessage());
    }

    //认证异常
    @ExceptionHandler(value = AuthException.class)
    public Result<Object> processMethod(AuthException ex){
        log.error("异常信息：", ex);
        return new Result<>(CodeEnum.TOKEN_OVERDUE, ex.getMessage());
    }


    //其他异常
    @ExceptionHandler(value = Exception.class)
    public Result<Object> processMethod(Exception ex){
        log.error("异常信息：", ex);
        return new Result<>(CodeEnum.SYSTEM_ERROR, "服务访问异常");
    }

}

