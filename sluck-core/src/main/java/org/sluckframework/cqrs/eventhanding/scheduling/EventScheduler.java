package org.sluckframework.cqrs.eventhanding.scheduling;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * public event job
 *
 * Author: sunxy
 * Created: 2015-09-17 22:03
 * Since: 1.0
 */
public interface EventScheduler {
    /**
     * 计划在指定的时间发布事件,返回的 token 可以用于取消事件
     *
     * @param triggerDateTime The moment to trigger publication of the event
     * @param event           The event to publish
     * @return the token to use when cancelling the schedule
     */
    ScheduleToken schedule(DateTime triggerDateTime, Object event);

    /**
     * 计划在指定的时间发布事件,返回的 token 可以用于取消事件
     *
     * @param triggerDuration The amount of time to wait before publishing the event
     * @param event           The event to publish
     * @return the token to use when cancelling the schedule
     */
    ScheduleToken schedule(Duration triggerDuration, Object event);

    /**
     * 使用 token 取消将要发布的事件
     *
     * @param scheduleToken the token returned when the event was scheduled
     * @throws IllegalArgumentException if the token belongs to another scheduler
     */
    void cancelSchedule(ScheduleToken scheduleToken);
}
