<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
    <title>KCB 휴대폰 본인확인 테스트 1</title>
    <script type="text/javascript">
        function jsSubmit(){
            window.open("", "auth_popup", "width=430,height=640,scrollbars=yes");
            var form = document.forms["form1"];
            form.target = "auth_popup";
            form.submit();
        }
    </script>
</head>
<body>
    <h1>휴대폰 본인확인 요청</h1>
    <form name="form1" action="/kcb/phone_popup2" method="post">
        <input type="button" value="휴대폰 본인확인" onclick="jsSubmit();">
    </form>

    <!-- 인증 결과를 받기 위한 숨은 폼 -->
    <form name="kcbResultForm" method="post" style="display:none;">
        <input type="hidden" name="CP_CD">
        <input type="hidden" name="TX_SEQ_NO">
        <input type="hidden" name="RSLT_CD">
        <input type="hidden" name="RSLT_MSG">
        <input type="hidden" name="RSLT_NAME">
        <input type="hidden" name="RSLT_BIRTHDAY">
        <input type="hidden" name="RSLT_SEX_CD">
        <input type="hidden" name="RSLT_NTV_FRNR_CD">
        <input type="hidden" name="DI">
        <input type="hidden" name="CI">
        <input type="hidden" name="CI_UPDATE">
        <input type="hidden" name="TEL_COM_CD">
        <input type="hidden" name="TEL_NO">
        <input type="hidden" name="RETURN_MSG">
    </form>
</body>
</html>