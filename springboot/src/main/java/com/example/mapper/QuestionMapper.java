package com.example.mapper;

import com.example.entity.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {
    @Select("select id,course_id,use_type,stem,option_a,option_b,option_c,option_d,correct_answer from question_bank where course_id=#{courseId} and use_type=#{useType} order by rand() limit #{limit}")
    List<Question> randomByCourseAndType(@Param("courseId") Long courseId, @Param("useType") String useType, @Param("limit") int limit);

    @Insert("insert into task_question(task_id,question_id) values(#{taskId},#{questionId})")
    int bindTaskQuestion(@Param("taskId") Long taskId, @Param("questionId") Long questionId);

    @Select("select q.id,q.course_id,q.use_type,q.stem,q.option_a,q.option_b,q.option_c,q.option_d,q.correct_answer from task_question tq join question_bank q on tq.question_id=q.id where tq.task_id=#{taskId}")
    List<Question> questionsByTaskId(Long taskId);
}
