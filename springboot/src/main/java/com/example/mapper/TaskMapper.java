package com.example.mapper;

import com.example.entity.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {
    @Insert("insert into task(course_id,type,title,details,due_date,created_by) values(#{courseId},#{type},#{title},#{details},#{dueDate},#{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Task task);

    @Select("select id,course_id,type,title,details,due_date,created_by from task order by due_date asc")
    List<Task> findAll();

    @Select("select id,course_id,type,title,details,due_date,created_by from task where id=#{id}")
    Task findById(Long id);
}
