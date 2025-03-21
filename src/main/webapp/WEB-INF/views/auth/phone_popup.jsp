<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
    <title>휴대폰 본인확인 요청</title>
    <style>
        /* 페이지 전체 스크롤 가능하도록 설정 */
        html, body {
            height: 100%;
            margin: 0;
            padding: 0;
            overflow: auto; /* 자동 스크롤 */
        }

        /* 특정 영역을 스크롤 가능하게 만들고 싶다면 추가 */
        .scrollable-container {
            max-height: 90vh; /* 화면 높이의 90% */
            overflow-y: auto; /* 세로 스크롤 */
            padding: 20px;
            border: 1px solid #ccc;
        }
    </style>
    <script type="text/javascript">
        function requestPopup(){
            document.popupForm.action = "<%= request.getAttribute("POPUP_URL") %>";
            document.popupForm.method = "post";
            document.popupForm.submit();
        }
    </script>
</head>
<body>

<%
    String cpCd = (String) request.getAttribute("CP_CD");
    String mdlTkn = (String) request.getAttribute("MDL_TKN");
%>

<div class="scrollable-container">
    <form name="popupForm">
        <input type="hidden" name="tc" value="kcb.oknm.online.safehscert.popup.cmd.P931_CertChoiceCmd"/>
        <input type="hidden" name="cp_cd" value="<%= (cpCd != null ? cpCd : "") %>"/>
        <input type="hidden" name="mdl_tkn" value="<%= (mdlTkn != null ? mdlTkn : "") %>"/>
    </form>
</div>

<script>
    requestPopup();
</script>

</body>
</html>