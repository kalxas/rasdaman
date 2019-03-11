Instructions for maintainers:

The WCS-Client was implemented using Typescript which compiles to raw Javascript. Typescript was chosen to improve maintainability and the speed of development.
In order to continue the development you must install:
1. NodeJS and NPM - Used for installing Bower, Typescript and TSD
2. Typescript - Used for compiling *.ts files to *.js


In order to build the code :
1. Install NodeJS and NPM
2. Install TypeScript from npm: sudo npm install -g tsc.
3. Run tsc in the root directory of the project to compile the *.ts files. This will create main.js and main.js.map.

Commit to the repository any updated assets. Please do not forget to update the .gitignore.


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
