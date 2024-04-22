package shootingstar.var.config;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shootingstar.var.chat.scheduler.ChatFlushJob;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail chatFlushJobDetail() {
        return JobBuilder.newJob(ChatFlushJob.class)
                .withIdentity("chatFlushJob", "chat")
                .withDescription("Redis → MySQL Write-Back 채팅 메시지 플러시")
                .storeDurably(true)
                .build();
    }

    @Bean
    public Trigger chatFlushTrigger(JobDetail chatFlushJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(chatFlushJobDetail)
                .withIdentity("chatFlushTrigger", "chat")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 0/5 * * * ?")
                                .withMisfireHandlingInstructionDoNothing()
                )
                .build();
    }
}