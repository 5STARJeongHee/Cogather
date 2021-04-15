<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>    
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<c:choose>
	<c:when test="${empty list || fn:length(list) == 0 }">
		<script>
			alert("해당 정보가 삭제되거나 없습니다");
			history.back();
		</script>
	</c:when>
	
	<c:otherwise>
	
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>읽기</title>
</head>
<script>
	function openWin(){
		window.open('${list[0].kko_url}', "kakaoTalk", "width=1000, height=700, toolbar=no, menubar=no, scrollbars=no, resizable=yes" ); 

	}
	function openMail(){
		location.href = 'mailSending';
	}
	
	
</script>
<link rel="stylesheet" href="/cogather/CSS/common.css">
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<script src="//code.jquery.com/jquery-1.11.0.min.js"></script>
<!-- lightbox2 제이퉈리 -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/lightbox2/2.11.1/css/lightbox.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath }/CSS/studyview.css">
<script src="https://cdnjs.cloudflare.com/ajax/libs/lightbox2/2.11.1/js/lightbox.min.js"></script>

<script>

function chkDelete(sg_id){
	// 삭제 여부, 다시 확인 하고 진행하기
	var r = confirm("삭제하시겠습니까?");
	
	if(r){
		location.href = 'studydeleteOk?sg_id=' + sg_id;
	}
} // chkDelete
function loadingPage(data){
	$("#filelist").text("");
}
function chkDeleteFile(sgf_id){
	// 삭제 여부, 다시 확인 하고 진행하기
	/* var r = confirm("삭제하시겠습니까?");
	
	if(r){
		location.href = 'studydeleteFileOk?sg_id=' + sg_id;
	} */
	
	$.ajax({
		url: "studydeleteFileOk?sgf_id=" + sgf_id,
		type: "GET",
		cache: false,
		success: function(data, status) {
			if (status == "success") {
				alert("삭제 성공");
				loadingPage(data);
			}
		}
	});
} // chkDelete

function fn_fileDown(sgf_id){
	var formObj = $("form[name='readForm']");
	$("#SGF_ID").attr("value", sgf_id);
	formObj.attr("action", "/cogather/group/fileDown");
	formObj.submit();  
//	location.href = '/cogather/group/fileDown';
	
}

lightbox.option({
    resizeDuration: 200,
    wrapAround: true,
    disableScrolling: false,
    fitImagesInViewport:false
})

function registerRoom(){
	$.ajax({
		url: "./MemberStudyRest/ms/${list[0].sg_id}/id2",
		type: "GET",
		cache: false,
		success: function(data, status) {
			if (status == "success") {
				alert("가입 신청하였습니다");
			}
		}
	});
}
function loadMember(){
	console.log("지나감");
	$.ajax({
		url: "./MemberStudyRest/${list[0].sg_id}",
		type: "GET",
		cache: false,
		success: function(data, status) {
			if (status == "success") {
				if(data.status == "OK"){
					updateMemberList(data);	
				}
				
			}
		}
	});
}



function updateMemberList(data){
	var result = "";
	var id;
	result += "<h4>현제 가입자</h4>"
	for(var i =0; i < data.rdata.length; i++){
		result += "<li>" +"<img class='member-thumbnail'src='${pageContext.request.contextPath }/"+ data.rmember[i].pimg_url+"'>"+"<span class='member-name'>"+data.rdata[i].id+"</span></li>";
	}
	result+="<hr>";
	result += "<h4>가입 신청중</h4>"
	for(var i = 0; i < data.cdata.length; i++){
		id=data.cmember[i].id;
		result += "<li>" +"<img class='member-thumbnail' src='${pageContext.request.contextPath }/"+ data.cmember[i].pimg_url+"'>"+"<span class='member-name'>"+data.cdata[i].id+"</span>"+
		"<button btn-id='"+id+"'type='button' onclick='accept(this.btn-id)' class='btn btn-success'>수락</button></li>";
		
	}
	console.log(accept(id));
	console.log(id);
	$(".member-list ul").html(result);
}

function accept(id){
	/* console.log($(this).attr('btn-id'));
	console.log($(this).attr('type'));  */
	console.log("sdfsd"+id);
	console.log('지나감222');
	$.ajax({
		url: "./MemberStudyRest/ms/${list[0].sg_id}/"+id,
		type: "PUT",
		cache: false,
		success: function(data, status) {
			if (status == "success") {
				if(data.status=="OK"){
					alert("가입승인 완료");
				}
			}
		}
	});
}


</script>
<body>
<div id="wrap">
<div class="w3-top">
  <div class="w3-bar" id="myNavbar">
    <a class="w3-bar-item w3-button w3-hover-black w3-hide-medium w3-hide-large w3-right" href="javascript:void(0);" onclick="toggleFunction()" title="Toggle Navigation Menu">
      <i class="fa fa-bars"></i>
    </a>
    <a href="/cogather/group/studygroup" class="w3-bar-item w3-button">
    	<img src="/cogather/img/logo_cut.png" class="logo"  >
    </a>
    <a href="/cogather/group/studylist" class="w3-bar-item w3-button w3-hide-small">스터디목록</a>
    <a href="#" class="w3-bar-item w3-button w3-hide-small">스터디만들기</a>
    
    <a href="#" class="w3-bar-item w3-button w3-hide-small">마이페이지</a>
    <a href="#" class="w3-bar-item w3-button w3-hide-small w3-right w3-hover-red login">로그인</a>
  </div>

  <!-- Navbar on small screens -->
  <div id="navDemo" class="w3-bar-block w3-white w3-hide w3-hide-large w3-hide-medium">
    <a href="#" class="w3-bar-item w3-button studylist" onclick="toggleFunction()">스터디목록</a>
    <a href="#" class="w3-bar-item w3-button" onclick="toggleFunction()">스터디만들기</a>
    <a href="#" class="w3-bar-item w3-button" onclick="toggleFunction() login">마이페이지</a>
  </div>
</div>
<img src="/cogather/img/group/studygroupmain.jpg" width="100%" height="300px" >
		<br><br>
	<div id="row" style=" display: -ms-flexbox; /* IE10 */display: flex;-ms-flex-wrap: wrap; /* IE10 */flex-wrap: wrap;">
		<div id="text_content">
		<b id="name_id" style="color:#ffd43b;float:middle;"><br>스터디 이름: </b><b>${list[0].sg_name }</b><br>
		<b id="name_id" style="color:#ffd43b;float:middle;">스터디 주제 : </b> <b>${list[0].sg_tag }</b><br>
		<b id="name_id" style="color:#ffd43b;float:middle;">인원수: </b><b>${list[0].sg_max }</b><br> 
		<b id="name_id" style="color:#ffd43b;float:middle;">스터디 소개: </b><br>
		
		<hr>
		<div>
		${list[0].sg_info }
		</div>
		<section id="container">
		<b id="name_id" style="color:#ffd43b;float:middle;">이미지 보기: </b><b> </b><br> 
		<!-- light box2  제이쿼리 -->
		<hr>
		<c:if test="${list[0].file_name ne null}" >
		<a href="/cogather/img/group/upload/${list[0].file_name}" data-lightbox="example-set">
			<img src="/cogather/img/group/upload/${list[0].file_name}" width="20%" height="50px">(이미지 클릭시 확대)
			</a>
		
	 			<%-- ${list[0].fileName}
			 --%>
		</c:if>
		<br><br><br>
		<form name="readForm" role="form" method="post">
		
			<input type="hidden" id="SG_ID" name="SG_ID" value="">
			<input type="hidden" id="SGF_ID" name="SGF_ID" value="">
		
		</form>
		<div style="border: 1px solid #dbdbdb;">
			
			<c:forEach var="file" items="${files}">
				<a href="#" onclick="fn_fileDown('${file.SGF_ID}'); return false;">
				<div id ="filelist">
				(${file.SGF_ID})${file.SGF_ORG_FILE_NAME}</a>
				
				<button style="background-color:white;border:0px" onclick="chkDeleteFile(${file.SGF_ID })"><img src="/cogather/img/group/yellowx.png" class="search" ></button>
				</div>
			</c:forEach>
			
		<%-- 	<a href="#" onclick="fn_fileDown('${file[0].sg_id}'); return false;">FF${file[0].sg_id}</a>${file[0].sgf_file_size}<br>
		 --%>	
		</div>
		
		<br><!-- location.href='${list[0].kko_url} -->
		<button type="submit" onclick="openWin();" style="background-color:white;border:0px"><img src="/cogather/img/group/kakao.png"></button>
		<button type="submit"  onclick="openMail()" style="background-color:white;border:0px"><img src="/cogather/img/group/mail.png"></button>
		
		</div>
		
		<div id="main" class="member-list-container">
		<button onclick="loadMember()" class="viewbutton hover">참가자목록</button>
		<div class="member-list">
			<ul></ul>
		</div>
		<br><br><br><br>
			
		</div>
	</div>

	
		
	


	
		<br><br><br><br><br><br><br><br><br>
		
		<button onclick="location.href='studyupdate?sg_id=${list[0].sg_id }'" style="border :0;outline:0;color:white;width:100px;height:50px;position:relative;float:right;left:-50%;
	margin:0 10px 0 0;" class="viewbutton hover">수정하기</button>
		<button onclick="location.href='studylist'" class="listbutton hover">목록보기</button>
		<button onclick="chkDelete(${list[0].sg_id })" style="background-color:#ffd43b;border :0;outline:0;color:white;width:100px;height:50px;position:relative;float:right;left:-50%;
	margin:0 10px 0 0;" class="viewbutton hover">삭제하기</button>
		
		<button onclick="registerRoom()" class="enterbutton hover" style="color:white;float:right;background-color:#ffd43b;border :0;
	outline:0;width:100px;
	height:50px;">방입장</button> 
	</section>
	
</body>
</html>
				
	</c:otherwise>
</c:choose>
