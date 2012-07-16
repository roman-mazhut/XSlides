<%@ include file="static/taglibs.resource" %>
<!DOCTYPE html>
<html>
<head>
<%@ include file="static/resources.resource" %>
<title>Login</title>
</head>
<body>
	<div class="container">
		<%@ include file="static/menu.resource" %>
		<div class="row">
			<div class="span4 offset4">
				<form class="well" method="POST" action="forgotPassword">
					<c:if test="${message != null}"><div class="alert alert-error">${message}</div></c:if>
					<label>Email</label> 
					<input id="email" type="text" class="span3" placeholder="Type your email..." name="email" />
					<div align="center"><button type="submit" class="btn">Send</button></div>
				</form>
			</div>
		</div>
	</div>
</body>
</html>