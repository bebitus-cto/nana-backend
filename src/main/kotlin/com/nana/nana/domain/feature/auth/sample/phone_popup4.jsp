<%@ page language="java" contentType="text/html; charset=euc-kr" pageEncoding="euc-kr"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=euc-kr" />
<%
    //**************************************************************************
	// 파일명 : phone_popup4.jsp
	// - 바닥페이지
	// 휴대폰 본인확인 서비스 결과 완료 화면
	//**************************************************************************
	
	//request.setCharacterEncoding("UTF-8"); // UTF-8 환경의 경우 주석 제거

	/* 공통 리턴 항목 */
	String CP_CD		= request.getParameter("CP_CD");					// 고객사코드
	String TX_SEQ_NO	= request.getParameter("TX_SEQ_NO");				// 거래번호
	String RSLT_CD		= request.getParameter("RSLT_CD");					// 결과코드
	//String RSLT_MSG	= request.getParameter("RSLT_MSG");					// 결과메세지(UTF-8)
	String RSLT_MSG	= new String(request.getParameter("RSLT_MSG").getBytes("8859_1"));	// 결과메세지(EUC-KR)
	
	//String RSLT_NAME		= request.getParameter("RSLT_NAME");			// 성명(UTF-8)
	String RSLT_NAME		= new String(request.getParameter("RSLT_NAME").getBytes("8859_1"));	// 성명(EUC-KR)
	String RSLT_BIRTHDAY	= request.getParameter("RSLT_BIRTHDAY");		// 생년월일
	String RSLT_SEX_CD 		= request.getParameter("RSLT_SEX_CD");			// 성별코드
	String RSLT_NTV_FRNR_CD = request.getParameter("RSLT_NTV_FRNR_CD");		// 내외국인 구분
	
	String DI = request.getParameter("DI");							// DI 값
	String CI = request.getParameter("CI");							// CI 값 (CI_UPDATE가 홀수일 경우 사용)
	String CI_UPDATE = request.getParameter("CI_UPDATE");			// CI 업데이트 횟수 (현재 ‘1’ 로 고정임)
	String TEL_COM_CD = request.getParameter("TEL_COM_CD");			// 통신사코드
	String TEL_NO = request.getParameter("TEL_NO");					// 휴대폰번호
	
	String RETURN_MSG = request.getParameter("RETURN_MSG");			// 리턴메시지
%>
<title>KCB 휴대폰 본인확인 서비스 샘플 4</title>
</head>
<body>
<h3>인증결과</h3>
<ul>
  <li>고객사코드		: <%=CP_CD%> </li>
  <li>거래번호			: <%=TX_SEQ_NO%> </li>
  <li>결과코드			: <%=RSLT_CD%></li>
  <li>결과메세지		: <%=RSLT_MSG%></li>

  <li>성명				: <%=RSLT_NAME%></li>
  <li>생년월일			: <%=RSLT_BIRTHDAY%></li>
  <li>성별				: <%=RSLT_SEX_CD%></li>
  <li>내외국인 구분		: <%=RSLT_NTV_FRNR_CD%></li>
  
  <li>DI				: <%=DI%></li>
  <li>CI				: <%=CI%></li>
  <li>CI업데이트 횟수	: <%=CI_UPDATE%></li>
  <li>통신사코드		: <%=TEL_COM_CD%></li>
  <li>휴대폰번호		: <%=TEL_NO%></li>
  
  <li>리턴메시지		: <%=RETURN_MSG%></li>

</ul>
<br/>
* 성별 - M:남, F:여
<br/>
* 내외국인구분 - L:내국인, F:외국인
<br/>
* 통신사 - 01:SKT, 02:KT, 03:LGU+, 04:SKT알뜰폰, 05:KT알뜰폰, 06:LGU+알뜰폰
</body>
</html>
