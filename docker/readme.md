# 스프링 프레임워크 프로젝트를 docker 배포해보기
## 목차
1. [컨테이너 구성](https://github.com/HuiEun-Lim/Cogather/blob/master/docker/readme.md#1-%EC%BB%A8%ED%85%8C%EC%9D%B4%EB%84%88-%EA%B5%AC%EC%84%B1)
2. [docker-compose 파일 설명](https://github.com/HuiEun-Lim/Cogather/blob/master/docker/readme.md#2-docker-compose-%ED%8C%8C%EC%9D%BC-%EC%84%A4%EB%AA%85)
3. [docker-compose 배포 과정](https://github.com/HuiEun-Lim/Cogather/blob/master/docker/readme.md#3-%EB%B0%B0%ED%8F%AC-%EA%B3%BC%EC%A0%95)
4. [aws로의 CI/CD]
5. [배포중 만난 문제들](https://github.com/HuiEun-Lim/Cogather/blob/master/docker/readme.md#4-%EB%B0%B0%ED%8F%AC%EC%A4%91-%EB%A7%8C%EB%82%9C-%EB%AC%B8%EC%A0%9C%EB%93%A4)
---
### 1. 컨테이너 구성
1. tomcat 웹애플리케이션 서버 컨테이너
2. oracle 11g database 서버 컨테이너
3. redis 서버 컨테이너

**컨테이너 구성**
![docker 구성 그림](https://user-images.githubusercontent.com/46335198/117306838-002d0580-aebb-11eb-90fa-40293aac7eea.png)

### 2. docker-compose 파일 설명

위의 컨테이너 구성을 만들기 위해 docker compose를 이용했고 아래는 docker-compose.yml 설정 파일에 대한 설명이다

*docker-compose.yml*
```yaml
version: '3'
services:
  web:
#    build:
#      context: ./tomcat
#      dockerfile: Dockerfile # Dockerfile로 image를 빌드하고 사용하는 것인데 느려서 컨테이너를 따로 빌드하고 사용
    image: qhxmaoflr/cogather-tomcat
    container_name: cogather # 컨테이너 별칭
    networks: # docker에서 만든 별도의 private network
      - cogather-network
    ports:
      - "80:8080" # web server port : 컨테이너 내부 포트
    environment:
      - TZ="Asia/Seoul" # 시간에 대한 환경변수
    volumes:
      - ./webapps:/usr/local/tomcat/webapps # 배포할 파일을 두는 곳 volumes로 매핑해두어 파일만 갈아 끼울 수 있게 함
    depends_on: # 아래의 종속된 컨테이너가 실행된뒤에 실행됨
      - oracle-db
      - redis-chat
    restart: always
  oracle-db: # oracle db container 별칭
    image: wnameless/oracle-xe-11g-r2
    container_name: oracle-db
    networks:
      - cogather-network
    ports:
      - "12345:1521"
    volumes:
      - ./db:/docker-entrypoint-initdb.d # oracle 컨테이너가 실행될시 처음 실행하게 되는 초기화 부분으로 초기 테이블 생성과 같은 쿼리문을 두면 됨
    environment:
      - TZ="Asia/Seoul"
      - ORACLE_HOME=/u01/app/oracle/product/11.2.0/xe # 컨테이너 내부에서 해당 환경변수가 세팅이 안되어 있어서 리스터 확인하기가 안되서 세팅해줌
    restart: always
  redis-chat: # redis server container 별칭
    image: redis:latest
    container_name: redis-chat
    networks:
      - cogather-network
    ports: 
      - "6379:6379"
    restart: always
networks: # 네트워크 생성 -여러 가지 설정이 가능하지만 일단은 기본으로 둠
  cogather-network:

```

---
### 3. 배포 과정

eclipse IDE에서 로컬로 개발되었기 때문에 host 명이나 기타 포트에 대해 조금 수정이 필요했음

---
1. redis 서버에 대한 연결 정보를 변경

*SpringRedisConfig.java*
```java
...
...
@Bean // Redis server 연결을 위한 Redis Client Factory bean 생성
	public JedisConnectionFactory connectionFactory() {
		RedisStandaloneConfiguration connection = new RedisStandaloneConfiguration();
		connection.setHostName("redis-chat"); // - > 원래는 localhost 였지만 컨테이너로 바뀌면서 'redis-chat'으로 변경
		connection.setPort(6379); 
		return new JedisConnectionFactory(connection); 
	}
...
...

```
---
2. oracle db 서버에 대한 url 변경

*root-context.xml*

```xml
<beans:bean name="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
		<beans:property name="driverClassName" value="oracle.jdbc.driver.OracleDriver"/>
		<beans:property name="url" value="jdbc:oracle:thin:@oracle-db:1521:XE"/>
        <!-- jdbc:oracle:thin:@localhost:1521:XE -> jdbc:oracle:thin:@oracle-db:1521:XE --> 
		<beans:property name="username" value="cogather"/>
		<beans:property name="password" value="1234"/>
</beans:bean>
```
---
3. db 암호화
*pom.xml에 jasypt 암호화 라이브러리 추가*

```xml
...
<!-- 암호화 라이브러리 jasypt-->
		<dependency>  
			<groupId>org.jasypt</groupId>  
			<artifactId>jasypt-spring3</artifactId>  
			<version>1.9.3</version>  
		</dependency>
...
```

*resources/config 폴더 생성-> db.properties 추가*

http://www.jasypt.org/download.html 이 사이트에서 jasypt 프로젝트 파일을 받아서 terminal, cmd를 실행해서 ../jasypt-xx/bin의 위치로 이동 

./encrypt input="암호화할 비번" password="암호화에 쓰이는 키값" 
을 입력하면 아래와 같은 화면이 나온다. **OUTPUT 값**을 복사해둔다.
```bash
PS C:\DevRoot\jasypt-1.9.2\bin> ./encrypt input="hello" password="pass"

----ENVIRONMENT-----------------

Runtime: Oracle Corporation Java HotSpot(TM) 64-Bit Server VM 11.0.10+8-LTS-162



----ARGUMENTS-------------------

input: hello
password: pass



----OUTPUT----------------------

lpAo4nXfFP4Jwt9GAUmlgg==
```
* properties 파일 생성 *
아래의 설정파일을 생성, 코드 작성 후 암호화된 값을 넣어준다. 

.../resources/config/db.properties
```properties
db.driver = oracle.jdbc.driver.OracleDriver
db.url = jdbc:oracle:thin:@oracle-db:1521:XE
db.id = cogather
db.passward = ENC(**OUTPUT**값을 그대로 넣는다.)
```

*db 설정 변경 -> root-context.xml에 아래 코드 추가 및 변경*
```xml
...
<!-- 암호화 설정 객체 생성 -->
  <bean id="environmentVariablesConfiguration" class="org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig">  
    <property name="algorithm" value="PBEWithMD5AndDES" />  
    <property name="passwordEnvName" value="jPass" /> <!-- jPass는 passwordEnvName으로 환경변수 이름을 넣는다. 이렇게 분리하면 실제 환경변수를 볼수 없는한 github에 올려도 노출되지 -->
  </bean>

  <bean id="configurationEncryptor" class="org.jasypt.encryption.pbe.StandardPBEStringEncryptor">  
    <property name="config" ref="environmentVariablesConfiguration" />  
  </bean>  
  <bean id="propertyConfigurer" class="org.jasypt.spring3.properties.EncryptablePropertyPlaceholderConfigurer">  
    <constructor-arg ref="configurationEncryptor" />  
    <property name="locations">  
      <list>  
        <value>classpath:config/db.properties</value>
      </list>  
    </property>  
  </bean>
  <!-- 암호화 설정 객체 생성 end -->
...
...
	<!-- DataSource 객체  oracle-db는 host 이름으로 docker oracle container의 alias로 지정-->
  <beans:bean name="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <beans:property name="driverClassName" value="${db.driver}"/>
    <beans:property name="url" value="${db.url}"/>
    <beans:property name="username" value="${db.id}"/>
    <beans:property name="password" value="${db.passward}"/>
  </beans:bean>
...
```

위에서 암호화에 사용한 키값은 실제 환경변수에 있어야 하므로 
docker-compose.yml의 tomcat container에 추가 해둔다.

``` yaml
ports:
      - "80:8080" # web server port : 컨테이너 내부 포트
    environment:
      - TZ="Asia/Seoul" # 시간에 대한 환경변수
      - jPass=BRACE_PASS # jayspt 암호화 복호화 키값
```

---
4. 테이블을 세팅할 sql문을 써놓음
 
*./docker/db/cogatherDB.sql* 

추후 컴포즈에서 volumes로 연결하여 읽어감

--- 
5. 배포 파일 만들기
*프로젝트 최상위 폴더에서 build 명령어 수행*
pom.xml 파일이 보이는 곳이 최상위 디렉토리이다.

여기서 maven build 커맨드를 터미널에 입력해주자 (혹시 mvn 커맨드가 존재하지 않으면 환경변수에 mvn이 있는 위치의 bin 파일을 시스템 환경변수에 등록하자-없다면 설치까지)

```bash
mvn package
```
target 폴더에 xxx.war파일이 있다.

6. ./webapps 폴더에 배포 파일 넣기
*./webapps* 폴더에 프로젝트 배포파일인 war 파일 넣어주기

-> 도커 컨테이너 실행시 해당 war파일을 읽어서 tomcat에 올려줌

---
6. tomcat 컨테이너 만들기
*.docker/tomcat/Dockerfile*

qhxmaoflr/cogather-tomcat 이라는 별도의 톰캣 컨테이너를 만들기 위함인데, 사실 아무 tomcat 컨테이너를 사용했어도 무방했다.

---
7. docker-compose 실행 
   
컨테이너들을 하나로 뭉쳐서 실행을 한다.

compose 파일이 위치한 곳에서 

cogather\docker\tomcat> 
```console
sudo docker-compose up -d
```

클라우드에 올리고 싶다면 클라우드 서버에 docker를 설치하고 
해당 프로젝트 폴더를 받아서 *sudo docker-compose up -d*를 하면 된다. (위에서 변경한 설정은 반영되어 있지 않으니 변경하고 배포파일로 만든 뒤에 진행하면 된다.)

### 4. AWS로의 CI/CD 
CI 를 맡아 처리해줄 [travis](https://travis-ci.com/) 사용을 위해 구글링하여 travis 사이트에 github를 연동해두자

연동했다고 끝이 아니라 travis에서 CI를 위해 배포 파일을 생성하기 위한 설정파일을 만들어두어야 한다.

*.travis.yml*

프로젝트 최상단 폴더에 .travis.yml 파일을 만들고 아래의 설정들을 입력한다.

아래의 작업은 travis가 aws의 s3에 배포 파일들을 올려두고 codedeploy를 통해 EC2의 배포 위치까지 배포 파일을 이동시킨 후 배포 스크립트를 통해 실제 배포 작업을 하는 설정이다.

```yaml
language: java
jdk:
  - openjdk8

branches:
  only:
    - master
cache:
  directories:
    - "$HOME/.m2/repository"
script: "mvn package"
before_deploy: # 폴더 단위의 이동만 되기 때문에 배포파일을 zip으로 담고 폴더에 넣어두는 작업
  - mkdir -p before-deploy # zip으로 만들 파일들만 골라 담을 디렉토리 생성
  - cp scripts/*.sh before-deploy/
  - cp -r docker before-deploy/
  - cp appspec.yml before-deploy/
  - cp target/*.war before-deploy/
  - cd before-deploy && zip -r before-deploy * 
  - cd ../ && mkdir -p deploy
  - mv before-deploy/before-deploy.zip deploy/cogather-webservice.zip

deploy:
  - provider: s3
    access_key_id: $AWS_ACCESS_KEY # Travis repo settings 에 설정된 값
    secret_access_key: $AWS_SECRET_KEY # 위랑 동일한 곳에 설정됨
    bucket: springboot-practice-build-storage # s3 버킷
    region: ap-northeast-2
    skip_cleanup: true
    acl: private # zip 파일 접근을 private로
    local_dir: deploy # before_deploy에서 생성한 디렉토리
    wait-until-deployed: true

  - provider: codedeploy
    access_key_id: $AWS_ACCESS_KEY 
    secret_access_key: $AWS_SECRET_KEY
    bucket: springboot-practice-build-storage # s3 버킷
    key: cogather-webservice.zip # 빌드 파일을 압축해서 전달
    bundle_type: zip # 압축 확장자
    application: cogather-webservice # 웹콘솔에서 등록한 CodeDeploy 애플리케이션
    deployment_group: cogather-webservice-group # codedeploy 배포 그룹
    region: ap-northeast-2
    wait-until-deployed: true
    
notifications: # ci에 대한 결과가 메일로 날라옴
  email:
    recipients:
      - "qhxmaoflr@gmail.com"
```
codedeploy는 설정 파일이 하나 더 있다. appspec.yml로 codedeploy가 작업할 내용을 기술한다.

.travis.yml 위치에 appspec.yml 파일을 만들어 둔다.

*appspec.yml*
```yaml
version: 0.0
os: linux
files:
  - source: /
    destination: /home/ubuntu/app/step2/zip/
    overwrite: yes

permissions:
  - object: /
    pattern: "**"
    owner: ubuntu
    group: ubuntu
hooks:
  ApplicationStart:
    - location: deploy.sh
      timeout: 180
      runas: ubuntu
```
destination은 s3의 zip에 있는 파일 전부를 /home/ubuntu/app/step2/zip 폴더에 옮기는 작업 - **/home/ubuntu/app/step2/zip 가 배포할 곳에 존재해야된다.**

permissions는 배포 파일의 권한 설정

hooks는 배포 폴더가 이동이 된후 실행할 스크립트를 설정한다.

스크립트는 이것으로 끝났고 나머지는 실제 EC2,S3,codedeploy, travis를 세팅하면 된다.

```
AWS 해야할것 (구글링하세요.)

EC2 생성(aws 이미지로 하는게 좋음) -> aws 이미지로 생성시 aws 서비스를 위한 추가적인 설치는 별로 없다. -> /home/ubuntu/step2/zip 폴더 생성, docker 설치

IAM -> 사용자 1개 (S3, Codedeploy 풀 권한) - 키와 시크릿 키 받아두자, 역할(codedeploy 역할 추가, ec2-codedeploy 간의 역할 추가)

S3 버킷 생성

codedeploy 애플리케이션 생성, 배포그룹 생성
```

*travis-ci.com 설정*
위의 설정중 $AWS_ACCESS_KEY는 실제 aws에서 받아온 값을 travis에서 환경변수로 매핑해둔 것으로 iam 사용자의 키와 시크릿 키를 매핑해둔다. 또한 jasypt를 위한 환경변수도 넣어준다. 

아래 그림처럼 하면 된다.

![image](https://user-images.githubusercontent.com/46335198/128291578-7928da3b-d985-4783-a691-4388e9de0cd8.png)

github에 코드를 올려보자 

### 5. 배포중 만난 문제들
1. 절대 경로 문제 
   - 프로젝트 내부 코드들 중에 절대 경로를 사용하는 부분이 있었고 심지어 'C:\\'로 시작하는 것이 있었음. tomcat 컨테이너는 ubuntu가 base 이미지인 컨테이너이므로 해당 코드들을 전부 상대 경로로 수정함
   - 
2. 컨테이너와 프로젝트 연결
   - db와 redis에 대한 컨테이너들을 연결할 때 url이나 기타 설정들을 어떻게 해야 연결이 되는지 감이 안잡혔었음 
   -> compose 설정시 컨테이너들의 별칭이 compose가 생성하는 네트워크 내에서 공유가 되고 해당 별칭들은 생성된 private network ip를 매핑하고 있으므로 host 부분을 컨테이너 별칭으로 수정하면 됨
   
3. db 연결 문제
   - 컨테이너들 끼리 연결은 되어 insert 하는 쿼리가 들어가는 것은 확인 되지만 로그인 시 유저 정보를 select 하는 부분에서 connection refused 에러가 뜸 
   -> db와 커넥션을 위한 설정인 dataSource에 대한 bean이 servlet-context.xml, root-context.xml에 들어있었고 docker 용으로 수정했던 servlet-context.xml과 기존의 root-context.xml이 충돌이 났던 것이었고 servlet-context.xml의 bean을 삭제했음
   
4. 스터디룸내의 ajax 요청 오류
  - ajax를 PUT 방식으로 보내게 될 경우 body에 데이터가 실리기 때문에 ajax 요청에서 보내는 데이터의 형식을 json이라고 명시해주어야 했었다.
    
    studyroomMember.js 
    ``` javascript
    ...
    $.ajax({
		
		url: contextPath + "/group/MemberStudyRest/ms/acctime",
		type: "PUT",
		dataType: "json",
		contentType:'application/json;charset=utf-8',// ajax request 할 경우 body에 넘기는 데이터 타입이 먼지 명시
		data: JSON.stringify({ // ajax request 할 경우 body에 json object 넘기기
			"sg_id": sg_id ,
			"id" : id,
			"acctime" : time.toISOString()})
		,
		...
    ```
  - ajax의 PUT 요청을 받는 Controller도 데이터를 지금까지 받아온 방식이 아닌 @RequestBody를 앞에 붙여야 json으로 넘어온 데이터를 파싱해서 가져올 수 있다.
    
    RestContentController.java
    ``` java
    ...
    @PutMapping("")
	public AjaxResult update(@RequestBody @Valid ContentDTO dto) { // @RequestBody로 json으로 넘어온 데이터 받기
		AjaxResult result = new AjaxResult();
		int cnt = 0;
		StringBuffer message = new StringBuffer();
		String status = "FAIL";
    ...
    ```
