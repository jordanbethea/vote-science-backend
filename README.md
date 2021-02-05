# Vote Science

This is an app to implement and experiment with different voting methods.

### Long term Roadmap:

1. Allow Users to create accounts, create their own voting slates, and vote on slates. 
    Allow Users to designate their slates as public or private, and allow them to invite their friends to vote on them.
    Allow users to vote on slates using any or all of several different voting methods.
    Show charts/graphs of results, including comparisons of results across different voting methods.
    
2. Create interactive tutorials showing the steps and thought processes of strategic voting.
    Users would be shown a set of choices, maybe allow them to pick which ones they agree with, and then show how their choices affect the final result.

3. Allow users to create simulations of votes on slates using large numbers of generated voters and votes.  

 
Used the 
[Scala Play Angular Seed](http://bit.ly/2AStvhK) as the seed for the code.


* [Play Framework: 2.8.0](https://www.playframework.com/documentation/2.8.x/Home)
* [Angular: 8.x.x](https://angular.io/)
* [Angular CLI: 8.3.9](https://cli.angular.io/)


### Useful Info (for myself):
#### Accessing the h2 browser, instructions from stack overflow: 
To browse the contents of your database via the H2 Web Console, start both the web console and the Play application via the same Play console:

* (Different for scala 2.8)
* Start sbt in repl mode by just typing sbt
* Start the H2 browser server by typing h2-browser
* Start the play server, and the h2 db server by typing run. This should be in same console window
* Driver Class: org.h2.Driver JDBC URL: jdbc:h2:mem:play User Name: Password: 
