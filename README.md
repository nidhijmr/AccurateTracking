# AccurateTracking

## Introduction
AccurateTracking is a mobile application which is used to track the user’s current location as well as identifies any anomaly in the places which user visit in his day-to-day routine. It keeps a track of all the places the user has visited and shows if there is any deviation from the normal travelling activity.

## Features List
1.	Signup form for the new user to log into the app.
2.	Login form to log into the application using email and password.
3.	Display of the user’s current location.
4.	Display of the locations the user has visited on the current day along with showing the anomaly from his routine travel activity.

## Architecture/Flow Diagram
<img src="https://user-images.githubusercontent.com/32632834/42433602-fd973f50-8304-11e8-85cb-cc5fd875ad00.png" widht="600" height="350"></img>

## Technical Stack
1.	Device Platform – Android
2.	Tool Kit – Android Studio
3.	Cloud Services – AWS (Cognito, DynamoDB, Kinesis Streams, Lambda, Kinesis Data Analytics, Kinesis Statistical and Deviatoon Function) 
4.	AWS Mobile SDK is used to programmatically invoke AWS APIs to perform various application functionalities.
5.	Database – Amazon DynamoDB 
6.	Google Maps and GeoCoder API
7.	Android UI Framework
8.	Build tool – Gradle

## Application Screenshots

### Landing Page
<img src="https://user-images.githubusercontent.com/32632834/42433713-5ffd42b6-8305-11e8-9e27-23db5ad7a1d9.png" widht="700" height="400"></img>
### Login Form
<img src="https://user-images.githubusercontent.com/32632834/42433717-6268a3ce-8305-11e8-82ef-c36c48665ab8.png" widht="700" height="400"></img>
### User’s current address
<img src="https://user-images.githubusercontent.com/32632834/42433721-66ba35d2-8305-11e8-9058-6d98a27fd55a.png" widht="700" height="400"></img>
### Anomaly detection
<img src="https://user-images.githubusercontent.com/32632834/42433724-69463904-8305-11e8-8389-a5d018c5d170.png" widht="700" height="400"></img>
### Address shown when marker is clicked
<img src="https://user-images.githubusercontent.com/32632834/42433727-6b46962c-8305-11e8-984b-e3ae2281ec4d.png" widht="700" height="400"></img>
