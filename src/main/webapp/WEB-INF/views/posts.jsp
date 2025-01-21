<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <title>Posts</title>
</head>
<body>
    <h1>List of Posts</h1>
    <table border="1">
        <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Text</th>
                <th>Tags</th>
                <th>Comments</th>
                <th>Likes</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="post" items="${posts}">
                <tr>
                    <td>${post.id}</td>
                    <td>${post.name}</td>
                    <td>${post.text}</td>
                    <td>
                        <c:forEach var="tag" items="${post.tags}">
                            ${tag}<br/>
                        </c:forEach>
                    </td>
                    <td>
                        <c:forEach var="comment" items="${post.comments}">
                            <div>
                                <strong>${comment.id}</strong>: ${comment.text}
                            </div>
                        </c:forEach>
                    </td>
                    <td>${post.likes}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</body>
</html>