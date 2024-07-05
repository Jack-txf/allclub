package com.feng.subject.domain.job;

import com.feng.subject.domain.service.SubjectLikedDomainService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 同步点赞数据的任务（从redis中同步到MySQL中去）
 *
 * @author: txf
 * @date: 2024/1/8
 */
/*
1.xxl-job，首先要启动jobAdminApplication服务。
2.然后将Executor（执行任务用的），注册到jobAdminApplication服务上
3.最后编写我们的任务。
经过上面三步之后，xxl-job就能在spring容器里面找到Executor的bean对象，和XxlJob任务bean对象；
然后，在admin浏览器界面就可以新增执行器和任务，将他们弄在一起，就可以很好地在浏览器中管理他们（执行器和任务）了。

jobHandler就是用@XxlJob标注出来的。
执行器的配置要独占一个端口号。例如本例子的9999
 */
@Component
@Slf4j
public class SyncLikedJob {

    @Resource
    private SubjectLikedDomainService subjectLikedDomainService;

    /**
     * 同步点赞数据任务
     */
    @XxlJob("syncLikedJobHandler")
    public void syncLikedJobHandler() throws Exception {
        XxlJobHelper.log("syncLikedJobHandler.start");
        try {
            log.info("同步数据====start");
            subjectLikedDomainService.syncLiked(); // 同步数据
            log.info("同步数据====end----------");
        } catch (Exception e) {
            XxlJobHelper.log("syncLikedJobHandler.error" + e.getMessage());
        }
    }

}
