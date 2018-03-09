package org.hspconsortium.sandboxmanagerapi.metrics;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
@Profile("aws-metrics")
public class AWSCloudWatchMetricsAspect {

    @Autowired
    private CloudWatchMetricPublisher cloudWatchMetricPublisher;

    @Value("${cloud.aws.cloudwatch.namespace:other}")
    private String metricNamespace;

    @Around("@annotation(PublishAtomicMetric)")
    public Object publishAtomicMetric(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();

        Date now = new Date();

        Dimension dimension = new Dimension()
                .withName("Service")
                .withValue(joinPoint.getSignature().getDeclaringType().getSimpleName());

        MetricDatum datum = new MetricDatum()
                .withMetricName(joinPoint.getSignature().getName())
                .withUnit(StandardUnit.Count)
                .withValue((double) 1)
                .withDimensions(dimension)
                .withTimestamp(now);

        cloudWatchMetricPublisher.publish("SANDBOX/" + metricNamespace, datum);

        return proceed;
    }

}
