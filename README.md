
ע��

- com.stip.face   Face.pathΪ����Ĭ������·������Ҫ�޸�
- com.stip.dbase  Dbase���ݿ����������Ҫ�޸�




```java

com.stip.dbase

Connection getConn()

int addPerson(String desitination)

String updatePerson(String name,String desitination)

String findPerson(String name)


com.stip.face

int detection(String url)

void putFace(String url,String name)

String compareFace(String url)

Servlet�ӿ�

DbaseServlet

request.getParameter("method"); //the name od Dbase method

method:updatePerson,findPerson

request.getParameter("name"); //people's name
 
request.getParameter("destination"); //people's destination


FaceServlet

request.getPararment("method");//the name of Face method

method:addPerson,comparePerson

request.getParameter("name"); //people's name
 
request.getParameter("destination"); //people's destination

request.getParameter("image");//the url of people's face  

```

