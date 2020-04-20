Instructions for maintainers:

The WCS-Client uses TypeScript rather JavaScript directly. To compile the WCS-Client, the following dependencies are necessary:
 - npm - Node package manger:
    # CentOS
      $ sudo yum install npm
    # Debian / Ubuntu
      $ sudo apt-get install npm
 - tsc - Used for compiling TypeScript .ts files to JavaScript .js:
    $ sudo npm install -g typescript

Everytime a new feature/fix is added, one needs to compile from TypeScript to JavaScript to work in Web Browsers with the following command in the WCS-Client source folder (application/wcs-client):

$ tsc

OR

$ make

This will generate two important files in application/wcs-client/app/ows: main.js and main.js.map. They need to be included in the patch besides other added/updated files.

Folder structure:
- app: Contains the application's source code.
  |
  ---- assets:
  |  	|
  |		|---- components: Contains dependencies automatically downloaded by Bower. This directory is ignored by Git with exceptions specified in the .gitignore.
  | 	|---- css: Contains custom CSS files.
  |		|---- img: Contains local images used for the layout.
  |		|---- libs: Contains libraries that are not managed by Bower.
  |		|---- typings: Contains Typescript bindings for several of the libraries used here.
  |
  |---- src:
		|
		|---- common: Contains utility classes used all over the code.
		|	  |
		|	  |---- directives: Contains Angular directives that have a general scope.
		|
		|---- components: Contains AngularJS components. The WCS specification was used as a
		|				  starting point for splitting the application in independent components.
		|---- models: Contains Typescript classes that model the WCS domain.
