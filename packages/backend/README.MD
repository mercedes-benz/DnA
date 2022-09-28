
Development approach

Salient Architecture principle
1. Web Services oriented architecture - each functionality is exposed as REST API.
2. Domain driven design - domain expertise at the center of the application enables all stakeholders to comprehend using ubiquitous language.
3. Mid-Tiered Centric processing - Minimal use of db functions/procedures/constraints. Business logic and validations at application level rather than at db.
4. De-coupled UI and services tiers - Flexible deployments with freedom to scale , not tightly bound to specific device UI. Services can be connected to all types of devices.
5. Standards based security - Use of JWT and OAUTH tokens. Security code analyzers for identifying security bugs during development itself.



Middleware 
  - Domain Driven Design
  - Onion architecture
  - Reusability of domain logic which shares common functionality
  - Use of Generics to remove boilerplate code repeating
  - Object relational mapping - JPA ORM
  - Introduction of JSONB to store json type text objects and querying on them.
  
  
  Domain Driven Desing
    Web Endpoints
        Application -             Controllers                Assemblers           
        Domain -                  Services            Business Policies       Entities
        Infrastructure - 
test