package co.tunan.tucache.core.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Created by wangxudong on 2021/6/4.
 *
 * @version: 1.0
 * @modified :
 */
public class TuConditionProcess implements BeanFactoryAware {

    private BeanFactory beanFactory;

    public boolean accept(String conditionStr, Object rootObject, Method method, Object[] arguments) {

        if (StringUtils.isEmpty(conditionStr)
                || conditionStr.equals("true")) {

            return true;
        }
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new MethodBasedEvaluationContext(rootObject, method, arguments,
                new DefaultParameterNameDiscoverer());

        // 加入使用@符号访问bean能力
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));

        return parser.parseExpression(conditionStr).getValue(context, Boolean.class);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

        this.beanFactory = beanFactory;

    }
}
