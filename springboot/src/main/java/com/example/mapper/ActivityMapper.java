package com.example.mapper;

import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityMapper {
    @Insert("insert into homework_submission(homework_id,student_id,content,status,submitted_at) values(#{homeworkId},#{studentId},#{content},'SUBMITTED',now())")
    int submitHomework(@Param("homeworkId") Long homeworkId, @Param("studentId") Long studentId, @Param("content") String content);

    @Insert("insert into video_watch_record(video_id,student_id,watch_time,last_watched_at) values(#{videoId},#{studentId},#{watchTime},now()) on duplicate key update watch_time=watch_time+values(watch_time), last_watched_at=now()")
    int upsertVideoWatch(@Param("videoId") Long videoId, @Param("studentId") Long studentId, @Param("watchTime") Integer watchTime);

    @Insert("insert into exam_submission(exam_id,student_id,answers,score,is_passed,submitted_at) values(#{examId},#{studentId},#{answers},#{score},#{isPassed},now())")
    int submitExam(@Param("examId") Long examId, @Param("studentId") Long studentId, @Param("answers") String answers, @Param("score") Integer score, @Param("isPassed") boolean isPassed);

    @Insert("insert ignore into exam(id,course_id,title,total_score,pass_score,start_time,end_time) values(#{examId},#{courseId},#{title},100,60,now(),date_add(now(), interval 7 day))")
    int ensureExamExists(@Param("examId") Long examId, @Param("courseId") Long courseId, @Param("title") String title);

    @Select("select count(1) from homework_submission where homework_id=#{taskId} and student_id=#{studentId}")
    int countHomeworkSubmitted(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    @Select("select ifnull(watch_time,0) from video_watch_record where video_id=#{taskId} and student_id=#{studentId} limit 1")
    Integer videoWatchTime(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    @Select("select count(1) from exam_submission where exam_id=#{taskId} and student_id=#{studentId}")
    int countExamSubmitted(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    @Select("select u.id as studentId,u.name as studentName,ifnull(a.login_count,0) as loginCount,ifnull(a.video_minutes,0) as videoMinutes,ifnull(a.homework_submitted,0) as homeworkSubmitted,ifnull(a.avg_score,0) as avgScore from user u left join student_daily_activity a on u.id=a.student_id and a.activity_date=#{date} where u.role='STUDENT'")
    List<Map<String,Object>> activitySummary(LocalDate date);

    @Select("""
            select u.id as studentId,
                   ifnull(a.login_count,0) as loginCount,
                   ifnull(a.video_minutes,0) as videoMinutes,
                   ifnull(a.homework_submitted,0) as homeworkSubmitted,
                   ifnull(a.avg_score,0) as avgScore,
                   ifnull(ex.exam_count,0) as examCount,
                   ifnull(ex.pass_count,0) as examPassCount,
                   ifnull(vw.watch_time,0) as rawWatchSeconds
            from user u
            left join student_daily_activity a on u.id = a.student_id and a.activity_date = #{date}
            left join (
               select student_id, count(1) as exam_count, sum(case when is_passed=1 then 1 else 0 end) as pass_count
               from exam_submission where date(submitted_at)=#{date} group by student_id
            ) ex on u.id = ex.student_id
            left join (
               select student_id, sum(watch_time) as watch_time
               from video_watch_record where date(last_watched_at)=#{date} group by student_id
            ) vw on u.id = vw.student_id
            where u.role='STUDENT'
            """)
    List<Map<String, Object>> featureRowsByDate(LocalDate date);

    @Select("""
            select student_id as studentId,
                   login_count as loginCount,
                   video_minutes as videoMinutes,
                   homework_submitted as homeworkSubmitted,
                   avg_score as avgScore
            from student_daily_activity
            where activity_date between #{startDate} and #{endDate}
            """)
    List<Map<String, Object>> historicalFeatures(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Insert("insert into risk_record(student_id,calc_date,risk_score,risk_level,detail_json) values(#{studentId},#{date},#{riskScore},#{riskLevel},#{detailJson})")
    int saveRiskRecord(@Param("studentId") Long studentId, @Param("date") LocalDate date, @Param("riskScore") double riskScore, @Param("riskLevel") String riskLevel, @Param("detailJson") String detailJson);

    @Select("select risk_level as riskLevel,count(*) as count from user where role='STUDENT' group by risk_level")
    List<Map<String,Object>> riskDistribution();

    @Select("select calc_date as date, avg(risk_score) as avgRiskScore from risk_record group by calc_date order by calc_date desc limit 7")
    List<Map<String,Object>> riskTrend();

    @Select("select student_id as studentId, risk_level as riskLevel, risk_score as riskScore, calc_date as calcDate from risk_record order by id desc limit 10")
    List<Map<String,Object>> recentWarnings();
}
