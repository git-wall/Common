### **BFF (Backend For Frontend)** 
- is a design pattern that creates a dedicated backend service for each frontend application. This allows for better separation of concerns, improved performance, and easier maintenance. The BFF acts as an intermediary between the frontend and the backend services, providing a tailored API that meets the specific needs of the frontend application.
<br>
IN: Controller -> Presenter -> Service -> Repository -> DB
OUT: Entity -> Assembler/Mapper -> Presenter(UI) -> Controller

### **Service in Microservice Architecture**
- In a microservice architecture, a service is a self-contained unit of functionality that performs a specific business function. Each service is designed to be independently deployable and scalable, allowing for greater flexibility and resilience in the overall system. Services communicate with each other through APIs, and they can be developed using different programming languages and technologies.
<br>
IN: Controller -> Facade/UseCase -> Service -> Repository -> DB 
                                            -> ACL (Anti-Corruption Layer) -> Ambassador (Monitor Around External Service Call) -> External Service
OUT: Entity -> Assembler/Mapper -> DTO -> Controller

### **Service Return Type**
+ If simple it can reuse, service should return Entity then Assembler/Mapper to Response/DTO in layer (Controller/Presenter/Facade/UseCase)
+ If complex service should return DTO/Response