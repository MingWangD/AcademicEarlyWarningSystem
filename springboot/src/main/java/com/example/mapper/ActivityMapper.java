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

    @Select("select u.id as studentId,u.name as studentName,ifnull(a.login_count,0) as loginCount,ifnull(a.video_minutes,0) as videoMinutes,ifnull(a.homework_submitted,0) as homeworkSubmitted,ifnull(a.avg_score,0) as avgScore from user u left join student_daily_activity a on u.id=a.student_id and a.activity_date=#{date} where u.role='STUDENT'")
    List<Map<String,Object>> activitySummary(LocalDate date);

    @Insert("insert into risk_record(student_id,calc_date,risk_score,risk_level) values(#{studentId},#{date},#{riskScore},#{riskLevel})")
    int saveRiskRecord(@Param("studentId") Long studentId, @Param("date") LocalDate date, @Param("riskScore") double riskScore, @Param("riskLevel") String riskLevel);

    @Select("select risk_level as riskLevel,count(*) as count from user where role='STUDENT' group by risk_level")
    List<Map<String,Object>> riskDistribution();

    @Select("select calc_date as date, avg(risk_score) as avgRiskScore from risk_record group by calc_date order by calc_date desc limit 7")
    List<Map<String,Object>> riskTrend();

    @Select("select student_id as studentId, risk_level as riskLevel, risk_score as riskScore, calc_date as calcDate from risk_record order by id desc limit 10")
    List<Map<String,Object>> recentWarnings();
}
