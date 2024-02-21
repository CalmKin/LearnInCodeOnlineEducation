package com.learnincode.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.learnincode.base.exception.BusinessException;
import com.learnincode.search.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @description 课程索引管理接口实现
 */
@Slf4j
@Service
public class IndexServiceImpl implements IndexService {



 @Autowired
 RestHighLevelClient client;

 @Override
 public Boolean addCourseIndex(String indexName,String id,Object object) {
  String jsonString = JSON.toJSONString(object);
  IndexRequest indexRequest = new IndexRequest(indexName).id(id);
  //指定索引文档内容
  indexRequest.source(jsonString,XContentType.JSON);
  //索引响应对象
  IndexResponse indexResponse = null;
  try {
   indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
  } catch (IOException e) {
   log.error("添加索引出错:{}",e.getMessage());
   e.printStackTrace();
   throw new BusinessException("添加索引出错");
  }
  String name = indexResponse.getResult().name();
  System.out.println(name);

  // 如果索引添加成功 OR 更新成功，都算成功
  return name.equalsIgnoreCase("created") || name.equalsIgnoreCase("updated");

 }

 @Override
 public Boolean updateCourseIndex(String indexName,String id,Object object) {

  String jsonString = JSON.toJSONString(object);
  UpdateRequest updateRequest = new UpdateRequest(indexName, id);
  updateRequest.doc(jsonString, XContentType.JSON);
  UpdateResponse updateResponse = null;
  try {
   updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
  } catch (IOException e) {
   log.error("更新索引出错:{}",e.getMessage());
   e.printStackTrace();
   throw new BusinessException("更新索引出错");
  }
  DocWriteResponse.Result result = updateResponse.getResult();
  return result.name().equalsIgnoreCase("updated");

 }

 @Override
 public Boolean deleteCourseIndex(String indexName,String id) {

  //删除索引请求对象
  DeleteRequest deleteRequest = new DeleteRequest(indexName,id);
  //响应对象
  DeleteResponse deleteResponse = null;
  try {
   deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
  } catch (IOException e) {
   log.error("删除索引出错:{}",e.getMessage());
   e.printStackTrace();
   throw new BusinessException("删除索引出错");
  }
  //获取响应结果
  DocWriteResponse.Result result = deleteResponse.getResult();
  return result.name().equalsIgnoreCase("deleted");
 }
}
