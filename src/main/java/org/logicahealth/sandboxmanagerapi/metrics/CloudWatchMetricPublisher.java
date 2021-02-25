package org.logicahealth.sandboxmanagerapi.metrics;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CloudWatchMetricPublisher {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void publish(String namespace, MetricDatum metricDatum) {
        logger.info("publishing metric: " + namespace + ", " + metricDatum.getMetricName() + ", " + metricDatum.getValue());
        final AmazonCloudWatch cw =
                AmazonCloudWatchClientBuilder.defaultClient();

        PutMetricDataRequest request = new PutMetricDataRequest()
                .withNamespace(namespace)
                .withMetricData(metricDatum);

        PutMetricDataResult response = cw.putMetricData(request);
        logger.info("PutMetricDataResult response: " + response);
    }

}
