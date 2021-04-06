package com.project.cogather.domain;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.mybatis.spring.annotation.MapperScan;

public interface TestDAO {
	//전체 예약내역 SELECT
	List<CafeDTO> select();
	
	//새로운 예약 등록
	int insert(CafeDTO dto);
	int insert(String ID, String seat_id, LocalDateTime start_date, LocalDateTime end_date, String payment);
	
	//특정 예약내역 삭제하기
	int deleteByUid(int res_id);
	
	CafeDTO searchBySubject(String seat_id);
} //end DAO
