var EarthServerGenericClient = {};

/**
 * @ignore Just Inheritance Helper
 */
Function.prototype.inheritsFrom = function (parentClassOrObject) {
  if (parentClassOrObject.constructor == Function) {
    //Normal Inheritance
    this.prototype = new parentClassOrObject;
    this.prototype.constructor = this;
    this.prototype.parent = parentClassOrObject.prototype;
  }
  else {
    //Pure Virtual Inheritance
    this.prototype = parentClassOrObject;
    this.prototype.constructor = this;
    this.prototype.parent = parentClassOrObject;
  }
  return this;
};

/**
 * @ignore remove function for arrays
 */
Array.prototype.remove = function (from, to) {
  var rest = this.slice((to || from) + 1 || this.length);
  this.length = from < 0 ? this.length + from : from;
  return this.push.apply(this, rest);
};

/**
 * This function checks if this code is running is on a mobile platform.
 * @return true if mobile platform, false if not
 */
EarthServerGenericClient.isMobilePlatform = function () {
  var mobilePlatform = (function (a) {
    if (/android.+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge|maemo|midp|mmp|opera m(ob|in)i|palm(os)?|phone|p(ixi|re)\/|plucker|pocket|psp|symbian|treo|up\.(browser|link)|vodafone|wap|windows(ce|phone)|xda|xiino/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|awa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r|s)|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp(i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac(|\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt(|\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg(g|\/(k|l|u)|50|54|e\-|e\/|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-||o|v)|zz)|mt(50|p1|v)|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v)|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-|)|webc|whit|wi(g|nc|nw)|wmlb|wonu|x700|xda(\-|2|g)|yas\-|your|zeto|zte\-/i.test(a.substr(0, 4))) {
      return true
    } else {
      return false
    }
  })(navigator.userAgent || window.opera);

  return mobilePlatform;
};

/**
 * @ignore Helper for Events
 */
EarthServerGenericClient.getEventTarget = function (e) {
  e = e || window.event;
  return e.target || e.srcElement;
};

/**
 * @class Creates a light to enlighten the scene.
 * @param domElement - Dom element to append the light to.
 * @param index - Index of the light.
 * @param position - Position of the light (local coordinates)
 * @param radius - Radius of the light.
 * @param color - Color if the Light
 * @constructor
 */
EarthServerGenericClient.Light = function (domElement, index, position, radius, color) {
  var ambientIntensity = "1";
  var intensity = "3";
  var location = "0 0 0";

  if (position === undefined) {
    location = position;
  }
  if (radius === undefined) {
    radius = "8000";
  }
  if (color === undefined) {
    color = "1 1 1";
  }

  if (domElement !== undefined && domElement !== null) {
    var light = document.createElement("PointLight");
    light.setAttribute("id", "EarthServerGenericClient_Light_" + index);
    light.setAttribute("ambientIntensity", ambientIntensity);
    light.setAttribute("color", color);
    light.setAttribute("intensity", intensity);
    light.setAttribute("radius", radius);
    light.setAttribute("location", location);

    domElement.appendChild(light);
    light = null;
  }
};

/**
 * @class SceneManager is the main class of the unified client.
 * All scene models are registered in this class with the add() function.
 * The createScene() function creates a x3dom scene with all scene models.
 * The createUI() function creates the UI.
 */
EarthServerGenericClient.SceneManager = function () {
  var models = [];               //Array of scene models
  var modelLoadingProgress = []; //Array to store the models loading progress
  var totalLoadingProgress = 0;  //Value for the loading progress bar (all model loading combined)
  var baseElevation = [];        //Every Model has it's base elevation on the Y-Axis. Needed to change and restore the elevation.
  var progressCallback = undefined;//Callback function for the progress update.
  var annotationLayers = [];      //Array of AnnotationsLayer to display annotations in the cube
  var cameraDefs = [];            //Name and ID of the specified cameras. Format: "NAME:ID"
  var lights = [];                //Array of (Point)lights
  var lightInScene = false;       //Flag if a light should be added to the scene

  //Default cube sizes
  var cubeSizeX = 1000;
  var cubeSizeY = 1000;
  var cubeSizeZ = 1000;

  //Background
  var Background_groundAngle = "0.9 1.5 1.57";
  var Background_groundColor = "0.8 0.8 0.95 0.4 0.5 0.85 0.3 0.5 0.85 0.31 0.52 0.85";
  var Background_skyAngle = "0.9 1.5 1.57";
  var Background_skyColor = "0.8 0.8 0.95 0.4 0.5 0.85 0.3 0.5 0.85 0.31 0.52 0.85";

  /**
   * The maximum resolution in one axis of one scene model.
   * @default 2000
   * @type {number}
   */
  var maxResolution = 2000;

  /**
   * Enables/Disables the logging of Server requests, building of terrain etc.
   * @default false
   * @type {boolean}
   */
  var timeLog = false;

  /**
   * This variable contains the AxisLabel object.
   * This object manages the labels and its appearances on each axis.
   * @default null
   * @type {Object}
   */
  var axisLabels = null;

  /**
   * Return the size of the cube in the x axis
   * @returns {number}
   */
  this.getCubeSizeX = function () {
    return cubeSizeX;
  };

  /**
   * Return the size of the cube in the y axis
   * @returns {number}
   */
  this.getCubeSizeY = function () {
    return cubeSizeY;
  };

  /**
   * Return the size of the cube in the z axis
   * @returns {number}
   */
  this.getCubeSizeZ = function () {
    return cubeSizeZ;
  };

  /**
   * Sets if a light is inserted into the scene.
   * @param value - Boolean value.
   */
  this.addLightToScene = function (value) {
    lightInScene = value;
  };

  /**
   * Returns the number of scene lights.
   * @returns {Number}
   */
  this.getLightCount = function () {
    return lights.length;
  };

  /**
   * This function sets the background of the X3Dom render window. The Background is basically a sphere
   * where the user can sets colors and defines angles to which the colors float.
   * Colors are RGB with floats [0-1] separated by whitespaces. ( "0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9" )
   * Angles are in [0-1.57] (1.57 is PI/2) and also separated by whitespaces. ( "0.9 1.57" )
   * You need exactly one more color than angles like the examples.
   * @param skyColors - Colors of the sky from top to horizon. Three RGB values for each color.
   * @param skyAngles - Angles to where the sky colors are drawn. 1.57 for full sky.
   * @param groundColors - Colors of the ground from bottom to horizon. Three RGB values for each color.
   * @param groundAngles - Angles to where the ground colors are drawn. 1.57 for full ground.
   */
  this.setBackground = function (skyColors, skyAngles, groundColors, groundAngles) {
    Background_groundAngle = groundAngles;
    Background_groundColor = groundColors;
    Background_skyAngle = skyAngles;
    Background_skyColor = skyColors;
  };

  /**
   * Returns the number of registered scene models.
   * @returns {Number}
   */
  this.getModelCount = function () {
    return models.length;
  };

  /**
   * Returns the name of the scene model with the given index.
   * @param modelIndex - Index of the model.
   * @returns {String}
   */
  this.getModelName = function (modelIndex) {
    if (modelIndex < models.length) {
      return models[modelIndex].name;
    }
    else {
      return "No model with ID " + modelIndex;
    }
  };

  /**
   * Returns the X offset of the model with the given index.
   * @param modelIndex - Index of the model.
   * @returns {Number}
   */
  this.getModelOffsetX = function (modelIndex) {
    if (modelIndex < models.length) {
      return models[modelIndex].xOffset;
    }
    else {
      console.log("MainScene::getModelOffsetX: No model with ID " + modelIndex);
      return 0;
    }
  };

  /**
   * Returns the Y offset of the model with the given index.
   * @param modelIndex - Index of the model.
   * @returns {Number}
   */
  this.getModelOffsetY = function (modelIndex) {
    if (modelIndex < models.length) {
      return models[modelIndex].yOffset;
    }
    else {
      console.log("MainScene::getModelOffsetY: No model with ID " + modelIndex);
      return 0;
    }
  };

  /**
   * Returns the Z offset of the model with the given index.
   * @param modelIndex - Index of the model.
   * @returns {Number}
   */
  this.getModelOffsetZ = function (modelIndex) {
    if (modelIndex < models.length) {
      return models[modelIndex].zOffset;
    }
    else {
      console.log("MainScene::getModelOffsetZ: No model with ID " + modelIndex);
      return 0;
    }
  };

  /**
   * Returns the transparency of the model with the given index.
   * @param modelIndex - Index of the model.
   * @returns {Number}
   */
  this.getModelTransparency = function (modelIndex) {
    if (modelIndex < models.length) {
      return models[modelIndex].transparency;
    }
    else {
      console.log("MainScene::getModelTransparency: No model with ID " + modelIndex);
      return 0;
    }
  };

  /**
   * Let the scene model set it's specific UI element in the given domElement.
   * @param modelIndex - Index of the model.
   * @param domElement - domElement to put the UI element into.
   */
  this.setSpecificElement = function (modelIndex, domElement) {
    if (modelIndex < models.length) {
      models[modelIndex].setSpecificElement(domElement);
    }
    else {
      console.log("MainScene::SetSpecificElement: No model with ID " + modelIndex);
    }
  };

  /**
   * @default 1000 / 200 on a mobile platform
   * @type {Number}
   */
  if (EarthServerGenericClient.isMobilePlatform())  //and for mobile Clients
    maxResolution = 200;

  /**
   * Enables or disables the logging.
   * @param value - Boolean
   */
  this.setTimeLog = function (value) {
    timeLog = value;
  };

  /**
   * Starts the timer for a logging event with the given name.
   * @param eventName
   */
  this.timeLogStart = function (eventName) {
    if (timeLog) {
      console.time(eventName);
    }
  };

  /**
   * Ends the timer for a logging event with the given name and prints the result.
   * @param eventName
   */
  this.timeLogEnd = function (eventName) {
    if (timeLog) {
      console.timeEnd(eventName);
    }
  };

  /**
   * Returns the index of a scene model with a given name.
   * @param modelName - Name of the model.
   * @returns {number} - Index of the model or -1 if no model with the given name was found.
   */
  this.getModelIndex = function (modelName) {
    for (var i = 0; i < models.length; i++) {
      if (models[i].name === modelName) {
        return i;
      }
    }

    return -1;
  };

  /**
   * Determines if an annotation layer will be drawn.
   * @param layerName - Name of the annotation layer.
   * @param drawValue - boolean value.
   */
  this.drawAnnotationLayer = function (layerName, drawValue) {
    var index = this.getAnnotationLayerIndex(layerName);
    if (index < annotationLayers.length) {
      annotationLayers[index].renderLayer(drawValue);
    }
    else {
      console.log("MainScene::drawAnnotationLayer: No Layer with name " + layerName);
    }
  };

  /**
   * Returns the annotation texts of a given annotation layer as an array of strings.
   * @param layerName - Name of the Annotation Layer.
   * @returns {*} - Array of Annotations as strings.
   */
  this.getAnnotationLayerTexts = function (layerName) {
    var index = this.getAnnotationLayerIndex(layerName);
    if (index < annotationLayers.length) {
      return annotationLayers[index].getAnnotationTexts();
    }
    else {
      var val = [];
      val.push("MainScene::getAnnotationLayerTexts: No Layer with name " + layerName);
      console.log(val);
      return val;
    }
  };

  /**
   * Returns the number of registered AnnotationLayers.
   * @returns {Number}
   */
  this.getAnnotationLayerCount = function () {
    return annotationLayers.length;
  };

  /**
   * Returns the name of the AnnotationLayer with the given index.
   * @param layerIndex - Index of the AnnotationLayer.
   * @returns {*} - Either the Name of the AnnotationLayer or "No Name"
   */
  this.getAnnotationLayerName = function (layerIndex) {
    if (layerIndex < annotationLayers.length) {
      return annotationLayers[layerIndex].name;
    }
    else {
      console.log("MainScene::getAnnotationLayerName: No Layer with ID " + layerIndex);
      return "No Name";
    }
  };

  /**
   * Returns the index of an existing AnnotationLayer in the array or -1 if no layer with the given name was found.
   * @param AnnotationLayerName - Name of the Layer
   * @returns {number} - Either index in the array or -1 if not found
   */
  this.getAnnotationLayerIndex = function (AnnotationLayerName) {
    for (var i = 0; i < annotationLayers.length; i++) {
      if (annotationLayers[i].name === AnnotationLayerName) {
        return i;
      }
    }

    return -1;
  };

  /**
   * Adds an AnnotationsLayer to the scene.
   * @param layerName - Name of the Layer. You need the name of a layer to add annotations to it.
   * @param modelName - Name of the scene model to bind the layer to. Can be empty if no binding is intended.
   * @param fontSize - Font size of all annotations added to this layer.
   * @param fontColor - Color of all annotations added to this layer.
   * @param fontHover - The annotation text hovers above the annotation marker by this value.
   * @param markerSize - The size if the annotation marker
   * @param markerColor - Color of the annotation marker
   */
  this.addAnnotationsLayer = function (layerName, modelName, fontSize, fontColor, fontHover, markerSize, markerColor) {
    var root = document.getElementById("AnnotationsGroup");
    if (root) {
      if (this.getAnnotationLayerIndex(layerName) < 0) {
        var layer = new EarthServerGenericClient.AnnotationLayer(layerName, root, fontSize, fontColor, fontHover, markerSize, markerColor);
        annotationLayers.push(layer);
        var modelIndex = this.getModelIndex(modelName);
        if (modelIndex >= 0) {
          models[modelIndex].addBinding(layer);
        }
      }
      else {
        console.log("AnnotationLayer with this name already created.");
      }
    }
    else {
      console.log("Please add Layers after creating the scene.");
    }
  };

  /**
   * Adds an annotation to an existing annotation layer.
   * @param AnnotationLayerName - Name of the annotation layer to add the annotation to.
   * @param xPos - Position on the x-axis of the annotation.
   * @param yPos - Position on the y-axis of the annotation.
   * @param zPos - Position on the z-axis of the annotation.
   * @param Text - Text of the annotation.
   */
  this.addAnnotation = function (AnnotationLayerName, xPos, yPos, zPos, Text) {
    var index = this.getAnnotationLayerIndex(AnnotationLayerName);
    if (index >= 0) {
      annotationLayers[index].addAnnotation(xPos, yPos, zPos, Text);
    }
    else {
      console.log("Could not found a AnnotationLayer with name: " + AnnotationLayerName);
    }
  };

  /**
   * Sets the callback function for the progress update. The progress function gives a parameter between 0-100.
   * You can set callback = null for no progress update at all. If no callback is given at all the progress is
   * printed to the console.
   * @param callback
   */
  this.setProgressCallback = function (callback) {
    progressCallback = callback;
  };

  /**
   * All Modules and Terrain shall report their loading progress.
   * Modules when they receive data and terrains if they are done building the terrain.
   * Every time this function is called 1 is added to the total progress. It is assumed that for every
   * request a terrain is build thus 100% = model.requests*2
   * If a callback is registered the function is called, otherwise the progress is printed to the console or ignored.
   * @param modelIndex - Index of the model.
   */
  this.reportProgress = function (modelIndex) {
    //If null no progress update is wished
    if (progressCallback !== null) {
      modelLoadingProgress[modelIndex] += 1;

      //Reset total loading progress to 0 and calc it with the new value
      totalLoadingProgress = 0;
      for (var i = 0; i < modelLoadingProgress.length; i++) {
        var tmp = modelLoadingProgress[i] / ( models[i].requests * 2 );
        if (tmp > 1.0) tmp = 1;
        totalLoadingProgress += tmp;
      }
      totalLoadingProgress = (totalLoadingProgress / modelLoadingProgress.length) * 100;

      //Callback function or console?
      if (progressCallback !== undefined) {
        progressCallback(totalLoadingProgress);
      }
      else {
        console.log(totalLoadingProgress);
      }
    }
  };

  /**
   * Returns the maximum resolution per dimension of a scene model.
   * This number depends on power templates (e.g. mobile device).
   * @return {Number}
   */
  this.getMaxResolution = function () {
    return maxResolution;
  };

  /**
   * Adds any scene model to the scene.
   * @param model - Any type of scene model.
   */
  this.addModel = function (model) {
    //Model ID is the current length of the models array. That means to IDs start at 0 and increase by 1.
    model.index = models.length;
    //Store model in the array
    models.push(model);
    //Initialize it's loading progress to 0
    modelLoadingProgress[model.index] = 0;
  };

  /**
   * Sets the view of the X3Dom window to the predefined camera.
   * @param camID - ID of the Camera dom object.
   */
  this.setView = function (camID) {
    var cam = document.getElementById(camID);
    if (cam) {
      //If the user changes the camera, then moves around the camera has to be set to false to be able to bin again
      cam.setAttribute('set_bind', 'false');
      cam.setAttribute('set_bind', 'true');
    }
  };

  /**
   * Returns the number of defined cameras
   * @returns {Number}
   */
  this.getCameraDefCount = function () {
    return cameraDefs.length;
  };

  /**
   * Returns the definition of the camera with the given index.
   * Format: "CameraName:CameraID"
   * CameraName is for the UI (show on a button or label)
   * CameraID is the ID of the dom element
   * @param cameraIndex - Index of the camera.
   * @returns {String}
   */
  this.getCameraDef = function (cameraIndex) {
    if (cameraIndex < cameraDefs.length) {
      return cameraDefs[cameraIndex];
    }
    else {
      return "Camera:NotDefined"
    }
  };

  /**
   * Creates the whole X3DOM Scene in the fishtank/cube with all added scene models.
   * The Sizes of the cube are assumed as aspect ratios with values between 0 and 1.
   * Example createScene("x3dom_div",1.0, 0.3, 0.5 ) Cube has 30% height and 50 depth compared to the width.
   * @param x3dID - ID of the x3d dom element.
   * @param sceneID - ID of the x3dom scene element.
   * @param SizeX - width of the cube.
   * @param SizeY - height of the cube.
   * @param SizeZ - depth of the cube.
   */
  this.createScene = function (x3dID, sceneID, SizeX, SizeY, SizeZ) {
    if (SizeX <= 0 || SizeX > 1.0) SizeX = 1.0;
    if (SizeY <= 0 || SizeY > 1.0) SizeY = 1.0;
    if (SizeZ <= 0 || SizeZ > 1.0) SizeZ = 1.0;

    cubeSizeX = (parseFloat(SizeX) * 1000);
    cubeSizeY = (parseFloat(SizeY) * 1000);
    cubeSizeZ = (parseFloat(SizeZ) * 1000);

    var scene = document.getElementById(sceneID);
    if (!scene) {
      alert("No X3D Scene found with id " + sceneID);
      return;
    }

    // Light
    if (lightInScene) {
      var lightTransform = document.createElement("transform");
      lightTransform.setAttribute("id", "EarthServerGenericClient_lightTransform0");
      lightTransform.setAttribute("translation", "0 0 0");
      lights.push(new EarthServerGenericClient.Light(lightTransform, 0, "0 0 0"));
      scene.appendChild(lightTransform);
    }

    // Background
    var background = document.createElement("Background");
    background.setAttribute("groundAngle", Background_groundAngle);
    background.setAttribute("groundColor", Background_groundColor);
    background.setAttribute("skyAngle", Background_skyAngle);
    background.setAttribute("skyColor", Background_skyColor);
    scene.appendChild(background);

    // Cameras
    var cam1 = document.createElement('Viewpoint');
    cam1.setAttribute("id", "EarthServerGenericClient_Cam_Front");
    cam1.setAttribute("position", "0 0 " + cubeSizeZ * 2);
    cameraDefs.push("Front:EarthServerGenericClient_Cam_Front");

    var cam2 = document.createElement('Viewpoint');
    cam2.setAttribute("id", "EarthServerGenericClient_Cam_Top");
    cam2.setAttribute("position", "0 " + cubeSizeY * 2.5 + " 0");
    cam2.setAttribute("orientation", "1.0 0.0 0.0 -1.55");
    cameraDefs.push("Top:EarthServerGenericClient_Cam_Top");

    var cam3 = document.createElement('Viewpoint');
    cam3.setAttribute("id", "EarthServerGenericClient_Cam_Side");
    cam3.setAttribute("position", "" + -cubeSizeX * 2 + " 0 0");
    cam3.setAttribute("orientation", "0 1 0 -1.55");
    cameraDefs.push("Side:EarthServerGenericClient_Cam_Side");

    scene.appendChild(cam1);
    scene.appendChild(cam2);
    scene.appendChild(cam3);

    // Cube
    var shape = document.createElement('Shape');
    var appearance = document.createElement('Appearance');
    var material = document.createElement('Material');
    material.setAttribute("emissiveColor", "1 1 0");

    var lineset = document.createElement('IndexedLineSet');
    lineset.setAttribute("colorPerVertex", "false");
    lineset.setAttribute("coordIndex", "0 1 2 3 0 -1 4 5 6 7 4 -1 0 4 -1 1 5 -1 2 6 -1 3 7 -1");

    var coords = document.createElement('Coordinate');
    coords.setAttribute("id", "cube");

    var cubeX = cubeSizeX / 2.0;
    var cubeY = cubeSizeY / 2.0;
    var cubeZ = cubeSizeZ / 2.0;
    var cubeXNeg = -cubeSizeX / 2.0;
    var cubeYNeg = -cubeSizeY / 2.0;
    var cubeZNeg = -cubeSizeZ / 2.0;

    var p = {};
    p[0] = "" + cubeXNeg + " " + cubeYNeg + " " + cubeZNeg + " ";
    p[1] = "" + cubeX + " " + cubeYNeg + " " + cubeZNeg + " ";
    p[2] = "" + cubeX + " " + cubeY + " " + cubeZNeg + " ";
    p[3] = "" + cubeXNeg + " " + cubeY + " " + cubeZNeg + " ";
    p[4] = "" + cubeXNeg + " " + cubeYNeg + " " + cubeZ + " ";
    p[5] = "" + cubeX + " " + cubeYNeg + " " + cubeZ + " ";
    p[6] = "" + cubeX + " " + cubeY + " " + cubeZ + " ";
    p[7] = "" + cubeXNeg + " " + cubeY + " " + cubeZ + " ";
    var points = "";
    for (var i = 0; i < 8; i++) {
      points = points + p[i];
    }
    coords.setAttribute("point", points);

    lineset.appendChild(coords);
    appearance.appendChild(material);
    shape.appendChild(appearance);
    shape.appendChild(lineset);
    scene.appendChild(shape);

    var trans = document.createElement('Transform');
    trans.setAttribute("id", "trans");
    scene.appendChild(trans);

    this.setView('EarthServerGenericClient_Cam_Front');
    this.trans = trans;

    var annotationTrans = document.createElement("transform");
    annotationTrans.setAttribute("id", "AnnotationsGroup");
    scene.appendChild(annotationTrans);
  };

  /**
   * Creates the axis labels around the cube.
   */
  this.createAxisLabels = function (xLabel, yLabel, zLabel) {
    //Use given parameters or default values if parameters are not defined
    xLabel = xLabel || "X";
    yLabel = yLabel || "Y";
    zLabel = zLabel || "Z";

    axisLabels = new EarthServerGenericClient.AxisLabels(cubeSizeX / 2, cubeSizeY / 2, cubeSizeZ / 2);
    axisLabels.createAxisLabels(xLabel, yLabel, zLabel);
  };

  /**
   * This function starts to load all models. You call this when the html is loaded or later on a click.
   */
  this.createModels = function () {
    for (var i = 0; i < models.length; i++) {
      models[i].createModel(this.trans, cubeSizeX, cubeSizeY, cubeSizeZ);
    }
  };

  /**
   * Updates the position of a light.
   * @param lightIndex - Index of the light
   * @param which - Which Axis will be changed (0:X 1:Y 2:Z)
   * @param value - the new position
   */
  this.updateLightPosition = function (lightIndex, which, value) {
    var trans = document.getElementById("EarthServerGenericClient_lightTransform" + lightIndex);

    if (trans && which !== undefined && value !== undefined) {
      var oldTrans = trans.getAttribute("translation");
      oldTrans = oldTrans.split(" ");
      oldTrans[which] = value;
      trans.setAttribute("translation", oldTrans[0] + " " + oldTrans[1] + " " + oldTrans[2]);
    }
    else {
      console.log("EarthServerGenericClient::SceneManager: Can't update light position.");
      console.log("Index " + lightIndex + ", Axis " + which + " and Position " + value);
    }
  };

  /**
   * Updates the radius of the light with the given index.
   * @param lightIndex - Index of the light.
   * @param value - New radius.
   */
  this.updateLightRadius = function (lightIndex, value) {
    var light = document.getElementById("EarthServerGenericClient_Light_" + lightIndex);
    if (light) {
      light.setAttribute("radius", value);
    }
    else {
      console.log("EarthServerGenericClient::SceneManager: Can't find light with index " + lightIndex + ".");
    }
  };

  /**
   * Updates the intensity of the light with the given index.
   * @param lightIndex - Index of the light.
   * @param value - New intensity.
   */
  this.updateLightIntensity = function (lightIndex, value) {
    var light = document.getElementById("EarthServerGenericClient_Light_" + lightIndex);
    if (light) {
      light.setAttribute("intensity", value);
    }
    else {
      console.log("EarthServerGenericClient::SceneManager: Can't find light with index " + lightIndex + ".");
    }
  };

  /**
   * Update Offset changes the position of the current selected SceneModel on the x-,y- or z-Axis.
   * @param modelIndex - Index of the model that should be altered
   * @param which - Which Axis will be changed (0:X 1:Y 2:Z)
   * @param value - The new position
   */
  this.updateOffset = function (modelIndex, which, value) {
    var trans = document.getElementById("EarthServerGenericClient_modelTransform" + modelIndex);
    var axis = "";

    if (trans) {
      var offset;
      switch (which) {
        case 0:
          offset = cubeSizeX / 2.0;
          axis = "xAxis";
          break;
        case 1:
          offset = cubeSizeY / 2.0;
          axis = "yAxis";
          break;
        case 2:
          offset = cubeSizeZ / 2.0;
          axis = "zAxis";
          break;
      }

      var oldTrans = trans.getAttribute("translation");
      oldTrans = oldTrans.split(" ");
      var delta = oldTrans[which] - (value - offset);
      oldTrans[which] = value - offset;
      trans.setAttribute("translation", oldTrans[0] + " " + oldTrans[1] + " " + oldTrans[2]);
      models[modelIndex].movementUpdate(axis, delta);
    }
  };

  /**
   * This changes the scaling on the Y-Axis(Elevation).
   * @param modelIndex - Index of the model that should be altered
   * @param value - The base elevation is multiplied by this value
   */
  this.updateElevation = function (modelIndex, value) {
    var trans = document.getElementById("EarthServerGenericClient_modelTransform" + modelIndex);

    if (trans) {
      var oldTrans = trans.getAttribute("scale");
      oldTrans = oldTrans.split(" ");

      if (baseElevation[modelIndex] === undefined) {
        baseElevation[modelIndex] = oldTrans[1];
      }

      oldTrans[1] = value * baseElevation[modelIndex] / 10;

      trans.setAttribute("scale", oldTrans[0] + " " + oldTrans[1] + " " + oldTrans[2]);
    }
  };

  /**
   * Changes the transparency of the Scene Model.
   * @param modelIndex - Index of the model that should be altered
   * @param value - New Transparency between 0-1 (Fully Opaque - Fully Transparent)
   */
  this.updateTransparency = function (modelIndex, value) {
    if (modelIndex < models.length) {
      models[modelIndex].updateTransparency(value);
    }
  };

  /**
   * This creates the UI for the Scene.
   * @param domElementID - The dom element where to append the UI.
   */
  this.createUI = function (domElementID) {
    EarthServerGenericClient.createBasicUI(domElementID);
  };

};

// Create main scene
EarthServerGenericClient.MainScene = new EarthServerGenericClient.SceneManager();//Namespace
var EarthServerGenericClient = EarthServerGenericClient || {};

/**
 * @class Abstract base class for scene models.
 */
EarthServerGenericClient.AbstractSceneModel = function () {
  /**
   * Sets the name of the scene model.
   * @param modelName - Name of the model.
   */
  this.setName = function (modelName) {
    this.name = String(modelName);
  };
  /**
   * Sets the area of interest for the model. (Lower Corner, Upper Corner)
   * @param minx - Minimum/Lower Latitude
   * @param miny - Minimum/Lower Longitude
   * @param maxx - Maximum/Upper Latitude
   * @param maxy - Maximum/Upper Longitude
   */
  this.setAreaOfInterest = function (minx, miny, maxx, maxy) {
    this.minx = minx;
    this.miny = miny;
    this.maxx = maxx;
    this.maxy = maxy;
  };
  /**
   * Sets the resolution of the scene model (if possible).
   * @param xRes - Resolution on the x-axis/Latitude
   * @param zRes - Resolution on the z-axis/Longitude
   */
  this.setResolution = function (xRes, zRes) {
    this.XResolution = parseInt(xRes);
    this.ZResolution = parseInt(zRes);

    var maxResolution = EarthServerGenericClient.MainScene.getMaxResolution();
    if (this.XResolution > maxResolution) {
      this.XResolution = maxResolution;
    }
    if (this.ZResolution > maxResolution) {
      this.ZResolution = maxResolution;
    }

  };

  /**
   * Sets the position of the scene model within the fishtank/cube. Values between [0-1]
   * @param xOffset - Offset on the x-axis/width  Default:0
   * @param yOffset - Offset on the y-axis/height Default:0
   * @param zOffset - Offset on the z-axis/depth  Default:0
   */
  this.setOffset = function (xOffset, yOffset, zOffset) {
    this.xOffset = parseFloat(xOffset);
    this.yOffset = parseFloat(yOffset);
    this.zOffset = parseFloat(zOffset);
  };
  /**
   * Sets the size of the scene model compared to the fishtank/cube. Values between 0 - 1.
   * @param xScale - Size of the model on the x-axis/width  Default:1   (whole cube)
   * @param yScale - Size of the model on the y-axis/height Default:0.3 (30% of the cube)
   * @param zScale - Size of the model on the x-axis/width  Default:1   (whole cube)
   */
  this.setScale = function (xScale, yScale, zScale) {
    this.xScale = parseFloat(xScale);
    this.yScale = parseFloat(yScale);
    this.zScale = parseFloat(zScale);
  };

  /**
   * Sets the image format for the server request.
   * @param imageFormat - Default "png".
   */
  this.setImageFormat = function (imageFormat) {
    this.imageFormat = String(imageFormat);
  };

  /**
   * Sets the initial transparency of the scene model.
   * The function accepts a parameter value in the range of 0 (fully opaque) and 1(fully transparent).
   * @param transparency - Value of transparency.
   */
  this.setTransparency = function (transparency) {
    this.transparency = parseFloat(transparency);
  };

  /**
   * Updates the transparency during runtime of the scene model.
   * The function accepts a value in the range of 0 (fully opaque) and 1(fully transparent).
   * @param transparency - Value of transparency.
   */
  this.updateTransparency = function (transparency) {
    this.terrain.setTransparency(transparency);
  };

  /**
   * Modules report their loading progress with this function which reports to the main scene.
   */
  this.reportProgress = function () {
    //The total progress of this module depends on the number of requests it does.
    //The progress parameter is the progress of ONE request.
    //ReceivedDataCount is the number of already received responses.
    //it is doubled because for each request one terrain will be build.
    var totalProgress = ((this.receivedDataCount) / (this.requests * 2)) * 100;
    EarthServerGenericClient.MainScene.reportProgress(this.index, totalProgress);
  };

  /**
   * Validates the received data from the server request.
   * Checks if a texture and a heightmap are available at the moment.
   * @param data - Received data from the server request.
   * @returns {boolean} - TRUE if OK, FALSE if some data is missing
   */
  this.checkReceivedData = function (data) {
    this.receivedDataCount++;
    this.reportProgress();

    if (data === null || !data.validate()) {
      alert(this.name + ": Request not successful.");
      this.reportProgress();//NO Terrain will be built so report the progress here
      this.removePlaceHolder();//Remove the placeHolder.

      //delete UI elements
      var header = document.getElementById("EarthServerGenericClient_ModelHeader_" + this.index);
      var div = document.getElementById("EarthServerGenericClient_ModelDiv_" + this.index);

      if (header && div) {
        var parent = div.parentNode;

        if (parent) {
          parent.removeChild(div);
          parent.removeChild(header);
        }
      }
      return false;
    }

    return true;
  };

  /**
   * Adds an Object that will be informed about movements and alterations of the model.
   * @param bindingObject - Object that will receive the notification.
   */
  this.addBinding = function (bindingObject) {
    for (var i = 0; i < this.bindings.length; i++) {
      if (this.bindings[i] == bindingObject) {
        console.log(this.name + "::addBinding: Object already registered.");
        return;
      }
    }
    this.bindings.push(bindingObject);
  };

  /**
   * Removes an Object that will be informed about movements and alterations of the model.
   * @param bindingObject - Object that will no longer receive the notification.
   */
  this.removeBinding = function (bindingObject) {
    for (var i = 0; i < this.bindings.length; i++) {
      if (this.bindings[i] === bindingObject) {
        this.bindings.remove(i);
        return;
      }
    }
  };

  /**
   * This function is called if the model is moved in the scene.
   * All bindings will also get the movement update.
   * @param movementType - Type of the movement: xAxis,zAxis,elevation...
   * @param value - Updated position
   */
  this.movementUpdate = function (movementType, value) {
    for (var i = 0; i < this.bindings.length; i++) {
      this.bindings[i].movementUpdate(movementType, value);
    }
  };

  /**
   * This creates a placeholder Element for the model. It consists of an simple quad.
   * Models that use this placeholder should remove it of course.
   */
  this.createPlaceHolder = function () {
    var appearance = document.createElement('Appearance');
    var material = document.createElement('Material');
    material.setAttribute("emissiveColor", "0.4 0.4 0.4");

    var trans = document.createElement('Transform');
    var yoff = (this.cubeSizeY * this.yOffset);
    trans.setAttribute("translation", "0 " + yoff + " 0");

    var shape = document.createElement('shape');
    var triangleset = document.createElement('IndexedFaceSet');
    triangleset.setAttribute("colorPerVertex", "false");
    triangleset.setAttribute("coordindex", "0 1 2 3 -1");

    var coords = document.createElement('Coordinate');

    var cubeX = this.cubeSizeX / 2.0;
    var cubeZ = this.cubeSizeZ / 2.0;
    var cubeXNeg = -this.cubeSizeX / 2.0;
    var cubeYNeg = -this.cubeSizeY / 2.0;
    var cubeZNeg = -this.cubeSizeZ / 2.0;

    var p = {};
    p[0] = "" + cubeXNeg + " " + cubeYNeg + " " + cubeZNeg + " ";
    p[1] = "" + cubeXNeg + " " + cubeYNeg + " " + cubeZ + " ";
    p[2] = "" + cubeX + " " + cubeYNeg + " " + cubeZ + " ";
    p[3] = "" + cubeX + " " + cubeYNeg + " " + cubeZNeg;

    var points = "";
    for (var i = 0; i < 4; i++) {
      points = points + p[i];
    }
    coords.setAttribute("point", points);

    triangleset.appendChild(coords);
    appearance.appendChild(material);
    shape.appendChild(appearance);
    shape.appendChild(triangleset);
    trans.appendChild(shape);

    this.placeHolder = trans;
    this.root.appendChild(this.placeHolder);

    appearance = null;
    material = null;
    shape = null;
    triangleset = null;
    coords = null;
    points = null;
    trans = null;
  };

  /**
   * Removes the PlaceHolder created in createPlaceHolder(). If already deleted nothing happens.
   */
  this.removePlaceHolder = function () {
    if (this.placeHolder !== null && this.placeHolder !== undefined) {
      this.root.removeChild(this.placeHolder);
      this.placeHolder = null;
    }
  };

  /**
   * Creates the transform for the scene model to fit into the fishtank/cube. This is done automatically by
   * the scene model.
   * @param xRes - Size of the received data on the x-axis (e.g. the requested DEM )
   * @param yRes - Size of the received data on the y-axis
   * @param zRes - Size of the received data on the z-axis
   * @param minvalue - Minimum Value along the y-axis (e.g. minimum value in a DEM, so the model starts at it's wished location)
   * @return {Element}
   */
  this.createTransform = function (xRes, yRes, zRes, minvalue) {
    var trans = document.createElement('Transform');
    trans.setAttribute("id", "EarthServerGenericClient_modelTransform" + this.index);

    this.YResolution = yRes;

    var scaleX = (this.cubeSizeX * this.xScale) / (parseInt(xRes) - 1);
    var scaleY = (this.cubeSizeY * this.yScale) / this.YResolution;
    var scaleZ = (this.cubeSizeZ * this.zScale) / (parseInt(zRes) - 1);
    trans.setAttribute("scale", "" + scaleX + " " + scaleY + " " + scaleZ);

    var xoff = (this.cubeSizeX * this.xOffset) - (this.cubeSizeX / 2.0);
    var yoff = (this.cubeSizeY * this.yOffset) - (minvalue * scaleY) - (this.cubeSizeY / 2.0);
    var zoff = (this.cubeSizeZ * this.zOffset) - (this.cubeSizeZ / 2.0);
    trans.setAttribute("translation", "" + xoff + " " + yoff + " " + zoff);

    return trans;
  };
  /**
   * Sets the default values. This is done automatically by the scene model.
   */
  this.setDefaults = function () {
    /**
     * Name of the model. This will be display in the UI.
     * @default Name is given by the module
     * @type {String}
     */
    this.name = "No name given";

    /**
     * All objects that are bound to the module. The will be noticed if the models is moved or altered.
     * Example: Annotation layers should be moved with the module and change the height when the elevation changes.
     * @type {Array}
     */
    this.bindings = [];

    /**
     * Resolution for the latitude.
     * @default 500
     * @type {Number}
     */
    this.XResolution = 500;

    /**
     * Resolution for the longitude
     * @default 500
     * @type {Number}
     */
    this.ZResolution = 500;

    /**
     * Offset on the X-Axis for the model.
     * @default 0
     * @type {Number}
     */
    this.xOffset = 0;

    /**
     * Offset on the Y-Axis for the model.
     * @default 0
     * @type {Number}
     */
    this.yOffset = 0;

    /**
     * Offset on the Z-Axis for the model.
     * @default 0
     * @type {Number}
     */
    this.zOffset = 0;

    /**
     * The models dimension compared to the whole cube on the X-Axis.
     * @default 1
     * @type {Number}
     */
    this.xScale = 1;

    /**
     * The models dimension compared to the whole cube on the Y-Axis.
     * @default 0.3
     * @type {Number}
     */
    this.yScale = 0.3;

    /**
     * The models dimension compared to the whole cube on the Z-Axis.
     * @default 1
     * @type {Number}
     */
    this.zScale = 1;

    /**
     * The used Image format (if one is used)
     * @default "png"
     * @type {String}
     */
    this.imageFormat = "png";

    /**
     * The amount of requests the model do. It is needed to keep track of the loading progress.
     * @default 1
     * @type {number}
     */
    this.requests = 1;

    /**
     * The amount of already received responses. Along with requests this is used to keep track of the loading progress.
     * @default 0
     * @type {number}
     */
    this.receivedDataCount = 0;

    /**
     * The Transparency of the model.
     * @default 0
     * @type {Number}
     */
    this.transparency = 0;
  };
};
/**
 * @class Builds one elevation grid chunk. It can consists of several elevation grids to be used in a LOD.
 * For every appearance in the appearances parameter one level is built with 25% size of the last level.
 * @param parentNode - Dom element to append the elevation grids to.
 * @param info - Information about the ID,position of the chunk, the heightmap's size and the modelIndex.
 * @param hf - The heightmap to be used for the elevation grid.
 * @param appearances - Array of appearances. For every appearance one level for LOD is built. 1 Level = no LOD.
 * @constructor
 */
function ElevationGrid(parentNode, info, hf, appearances) {
  "use strict";

  /**
   * Creates and inserts elevation grid (terrain chunk) into the DOM.
   */
  function setupChunk() {

    try {
      var elevationGrid, shape, shf;

      //We build one level of a LOD for every appearance. Example: With 3 children means: [Full Resolution, 1/2 Resolution, 1/4 Resolution]
      for (var i = 0; i < appearances.length; i++) {
        //All none full resolutions needs to be one element bigger to keep the desired length
        var add = 0;
        if (i !== 0) {
          add = 1;
        }

        //Set up: Shape-> Appearance -> ImageTexture +  Texturetransform
        shape = document.createElement('Shape');
        shape.setAttribute("id", info.modelIndex + "_shape_" + info.ID + "_" + i);

        //Build the Elevation Grids
        //shrink the heightfield to the correct size for this detail level
        shf = shrinkHeightMap(hf, info.chunkWidth, info.chunkHeight, Math.pow(2, i));
        elevationGrid = document.createElement('ElevationGrid');
        elevationGrid.setAttribute("id", info.modelIndex + "hm" + info.ID + "_" + i);
        elevationGrid.setAttribute("solid", "false");
        elevationGrid.setAttribute("xSpacing", String(Math.pow(2, i)));//To keep the same size with fewer elements increase the space of one element
        elevationGrid.setAttribute("zSpacing", String(Math.pow(2, i)));
        elevationGrid.setAttribute("xDimension", String(info.chunkWidth / Math.pow(2, i) + add));//fewer elements in every step
        elevationGrid.setAttribute("zDimension", String(info.chunkHeight / Math.pow(2, i) + add));
        elevationGrid.setAttribute("height", shf);
        elevationGrid.appendChild(calcTexCoords(info.xpos, info.ypos, info.chunkWidth, info.chunkHeight, info.terrainWidth, info.terrainHeight, Math.pow(2, i)));

        shape.appendChild(appearances[i]);
        shape.appendChild(elevationGrid);

        parentNode.appendChild(shape);

        //set vars null
        shf = null;
        shape = null;
        elevationGrid = null;
      }
      hf = null;
      parentNode = null;
      info = null;
      appearances = null;
    }
    catch (error) {
      alert('ElevationGrid::setupChunk(): ' + error);
    }
  }

  /**
   * Shrinks the heightfield with the given factor
   * @param heightfield - The used heihgfield.
   * @param sizex - Width of the heightfield.
   * @param sizey - Height of the heightfield.
   * @param shrinkfactor - Factor to shrink the heightmap. 1:Full heightmap 2: 25% (scaled 50% on each side)
   * @returns {string}
   */
  function shrinkHeightMap(heightfield, sizex, sizey, shrinkfactor) {
    var smallGrid, smallx, smally, val, i, k, l, o, div;

    smallGrid = [];
    smallx = parseInt(sizex / shrinkfactor);
    smally = parseInt(sizey / shrinkfactor);
    //IF shrunk, the heightfield needs one more element than the desired length (63 elements for a length of 62)
    if (shrinkfactor !== 1) {
      smallx++;
      smally++;
      div = shrinkfactor * shrinkfactor;

      for (i = 0; i < smally; i++) {
        var i_sf = (i * shrinkfactor);

        for (k = 0; k < smallx; k++) {
          var k_sf = (k * shrinkfactor);
          val = 0;
          for (l = 0; l < shrinkfactor; l++) {
            for (o = 0; o < shrinkfactor; o++) {
              var x = k_sf + l;
              var y = i_sf + o;
              if (x >= sizex) x = sizex - 1;
              if (y >= sizey) y = sizey - 1;
              var tmp = heightfield[y][x];
              val = val + parseFloat(tmp);
            }
          }
          val = val / div;
          smallGrid.push(val + " ");
        }
      }
    }
    else {
      for (i = 0; i < smally; i++) {
        for (k = 0; k < smallx; k++) {
          val = parseFloat(heightfield[i][k]);
          smallGrid.push(val + " ");
        }
      }
    }
    return smallGrid.join(" ");
  }

  /**
   * Calcs the TextureCoordinates for the elevation grid(s).
   * Use the values of the full/most detailed version if using for LOD and adjust only the shrinkfactor parameter.
   * @param xpos - Start position of the elevation grid within the terrain.
   * @param ypos - Start position of the elevation grid within the terrain.
   * @param sizex - Size of the elevation grid on the x-Axis.
   * @param sizey - Size of the elevation grid on the x-Axis.
   * @param terrainWidth - Size of the whole terrain on the x-Axis.
   * @param terrainHeight - Size of the whole terrain on the y-Axis.
   * @param shrinkfactor - The factor the heightmap this TextureCoordinates are was shrunk.
   * @returns {HTMLElement} - X3DOM TextureCoordinate Node.
   */
  function calcTexCoords(xpos, ypos, sizex, sizey, terrainWidth, terrainHeight, shrinkfactor) {
    var tc, tcnode, i, k, offsetx, offsety, partx, party, tmpx, tmpy, smallx, smally;
    offsetx = xpos / terrainWidth;
    offsety = ypos / terrainHeight;
    partx = parseFloat((sizex / terrainWidth) * (1 / sizex) * shrinkfactor);
    party = parseFloat((sizey / terrainHeight) * (1 / sizey) * shrinkfactor);
    smallx = parseInt(sizex / shrinkfactor);
    smally = parseInt(sizey / shrinkfactor);

    if (shrinkfactor !== 1) {
      smallx++;
      smally++;
    }

    var buffer = [];
    //Create Node
    tcnode = document.createElement("TextureCoordinate");

    //File string
    for (i = 0; i < smally; i++) {
      for (k = 0; k < smallx; k++) {
        tmpx = offsetx + (k * partx);
        tmpy = offsety + (i * party);

        buffer.push(tmpx + " ");
        buffer.push(tmpy + " ");
      }
    }
    tc = buffer.join("");

    tcnode.setAttribute("point", tc);

    return tcnode;
  }

  setupChunk();
}//Namespace
var EarthServerGenericClient = EarthServerGenericClient || {};

/**
 * @class Abstract base class for terrains.
 * @constructor
 */
EarthServerGenericClient.AbstractTerrain = function () {
  /**
   * Stores the created appearances' names.
   * @type {Array}
   */
  var AppearanceDefined = [];

  /**
   * Stores the IDs of the materials to change the transparency.
   * @type {Array}
   */
  this.materialNodes = [];//Stores the IDs of the materials to change the transparency.

  /**
   * Creates a html canvas element out of the texture and removes the alpha values.
   * @param texture - Texture to draw. Can be everything which can be rendered into a canvas.
   * @param index - Index of the model using this canvas. Used to give the canvas a unique ID.
   * @returns {HTMLElement} The canvas element.
   */
  this.createCanvas = function (texture, index) {
    var canvasTexture = null;

    if (texture !== undefined) {
      canvasTexture = document.createElement('canvas');
      canvasTexture.style.display = "none";
      canvasTexture.setAttribute("id", "EarthServerGenericClient_Canvas" + index);
      canvasTexture.width = Math.pow(2, Math.round(Math.log(texture.width) / Math.log(2)));
      canvasTexture.height = Math.pow(2, Math.round(Math.log(texture.height) / Math.log(2)));

      var context = canvasTexture.getContext('2d');
      context.drawImage(texture, 0, 0, canvasTexture.width, canvasTexture.height);

      var imageData = context.getImageData(0, 0, canvasTexture.width, canvasTexture.height);
      for (var i = 0; i < imageData.data.length; i += 4) {
        imageData.data[i + 3] = 255;
      }
      context.putImageData(imageData, 0, 0);
    }
    else {
      console.log("EarthServerGenericClient.AbstractTerrain: Could not create Canvas, response Texture is empty.");
    }

    return canvasTexture;
  };

  /**
   * Calcs the needed numbers of chunks for the terrain for a specific chunk size.
   * @param width - Width of the entire terrain.
   * @param height - Height of the entire terrain.
   * @param chunkSize - The size of one chunk.
   * @returns {} numChunksX: number, numChunksY: number, numChunks: number
   */
  this.calcNumberOfChunks = function (width, height, chunkSize) {
    var chunksInfo = {
      numChunksX: parseInt(width / chunkSize),
      numChunksY: parseInt(height / chunkSize),
      numChunks : 0
    };

    if (width % chunkSize !== 0) {
      chunksInfo.numChunksX++;
    }


    if (height % chunkSize !== 0) {
      chunksInfo.numChunksY++;
    }

    chunksInfo.numChunks = parseInt(chunksInfo.numChunksY * chunksInfo.numChunksX);
    return chunksInfo;
  };

  /**
   * This function calcs the needed information to build and place a chunk of a terrain.
   * @param index - Index of the model using the terrain. Used for creating IDs.
   * @param chunkSize - The desired size (count of values) of one chunk per axis.
   * @param chunkInfo - This parameter uses an object that will be returned by calcNumberOfChunks().
   *      It contains the information about a terrain and its chunks (e.g. number of chunks on each axis).
   * @param currentChunk - The index of the current chunk to be build.
   * @param terrainWidth - Width of the whole terrain. Used to calc texture coordinates.
   * @param terrainHeight - Height of the whole terrain. Used to calc texture coordinates.
   * @returns {}
   *      xpos: number, ypos: number, chunkWidth: number,
   *      chunkHeight: number, terrainWidth: number,
   *      terrainHeight: number, ID: number, modelIndex: number
   */
  this.createChunkInfo = function (index, chunkSize, chunkInfo, currentChunk, terrainWidth, terrainHeight) {
    var info = {
      xpos         : parseInt(currentChunk % chunkInfo.numChunksX) * (chunkSize - 1),
      ypos         : parseInt(currentChunk / chunkInfo.numChunksX) * (chunkSize - 1),
      chunkWidth   : 0,
      chunkHeight  : 0,
      terrainWidth : terrainWidth,
      terrainHeight: terrainHeight,
      ID           : currentChunk,
      modelIndex   : index
    };

    if (currentChunk % chunkInfo.numChunksX === (chunkInfo.numChunksX - 1)) {
      info.chunkWidth = terrainWidth - parseInt((chunkInfo.numChunksX - 1) * chunkSize);
    }
    else {
      info.chunkWidth = chunkSize;
    }

    if (currentChunk >= chunkInfo.numChunks - chunkInfo.numChunksX) {
      info.chunkHeight = terrainHeight - parseInt((chunkInfo.numChunksY - 1) * chunkSize);
    }
    else {
      info.chunkHeight = chunkSize
    }

    return info;
  };

  /**
   * Returns a height map part from the given height map specified in the info parameter.
   * @param info - Which part of the heightmap should be returned.
   * @returns {*}
   */
  this.getHeightMap = function (info) {
    try {
      var heightmapPart = new Array(info.chunkHeight);
      for (var i = 0; i < info.chunkHeight; i++) {
        heightmapPart[i] = new Array(info.chunkWidth);
        for (var j = 0; j < info.chunkWidth; j++) {
          //If the requested position is out of bounce return the min value of the hm.
          if (i > this.data.width || j > this.data.height || info.xpos + j < 0 || info.ypos + i < 0) {
            heightmapPart[i][j] = this.data.minHMvalue;
          }
          else {
            heightmapPart[i][j] = this.data.heightmap[info.xpos + j][info.ypos + i];
          }
        }
      }
      return heightmapPart;
    }
    catch (error) {
      console.log('AbstractTerrain::getHeightMap(): ' + error);
      return null;
    }
  };

  /**
   * Collects all material nodes of the terrain and changes each transparency attribute.
   * @param value - Transparency value between 0 (full visible) and 1 (invisible).
   */
  this.setTransparency = function (value) {
    for (var k = 0; k < this.materialNodes.length; k++) {
      var mat = document.getElementById(this.materialNodes[k]);
      if (mat !== null) {
        mat.setAttribute("transparency", value);
      }
      else {
        console.log("Material with ID " + this.materialNodes[k] + " not found.");
      }
    }
  };

  /**
   * Deletes all saved material IDs. Use this function if you remove old material from the dom.
   * E.g. for ProgressiveTerrain.
   */
  this.clearMaterials = function () {
    this.materialNodes = [];
  };


  /**
   * This function handles the creation and usage of the appearances. It can be called for every shape or LOD that should use a canvasTexture.
   * It returns the amount of appearances specified. For every name only one appearance exits, every other uses it.
   * @param AppearanceName - Name of the appearance. If this name is not set in the array, it will be registered.
   *      In the case the name is already set, the existing one will be used.
   * @param AppearanceCount - Number of appearance to be created. E.g. the LODs use a bunch of three appearance nodes.
   * @param modelIndex - Index of the model using this appearance.
   * @param canvasTexture - Canvas element to be used in the appearance as texture.
   * @param transparency - Transparency of the appearance.
   * @returns {Array} - Array of appearance nodes. If any error occurs, the function will return null.
   */
  this.getAppearances = function (AppearanceName, AppearanceCount, modelIndex, canvasTexture, transparency) {
    try {
      var appearances = [AppearanceCount];
      for (var i = 0; i < AppearanceCount; i++) {
        var appearance = document.createElement('Appearance');
        appearance.setAttribute('sortType', 'transparent');

        if (AppearanceDefined[AppearanceName] != undefined)//use the already defined appearance
        {
          appearance.setAttribute("use", AppearanceDefined[AppearanceName]);
        }
        else    //create a new appearance with the given parameter
        {
          AppearanceDefined[AppearanceName] = AppearanceName;
          appearance.setAttribute("id", AppearanceDefined[AppearanceName]);
          appearance.setAttribute("def", AppearanceDefined[AppearanceName]);

          var texture = document.createElement('Texture');
          texture.setAttribute('hideChildren', 'true');
          texture.setAttribute("repeatS", 'true');
          texture.setAttribute("repeatT", 'true');

          texture.appendChild(canvasTexture);

          var imageTransform = document.createElement('TextureTransform');
          imageTransform.setAttribute("scale", "1,-1");

          var material = document.createElement('material');
          material.setAttribute("specularColor", "0.25,0.25,0.25");
          material.setAttribute("diffuseColor", "1 1 1");
          material.setAttribute('transparency', transparency);
          material.setAttribute('ID', AppearanceName + "_mat");
          //Save this material ID to change transparency during runtime
          this.materialNodes.push(AppearanceName + "_mat");

          appearance.appendChild(material);
          appearance.appendChild(imageTransform);
          appearance.appendChild(texture);

          texture = null;
          imageTransform = null;
          material = null;
        }
        appearances[i] = appearance;
      }
      return appearances;
    }
    catch (error) {
      console.log('AbstractTerrain::getAppearances(): ' + error);
      return null;
    }
  };

  /**
   * Returns the Width of the Heightmap of the terrain.
   * @returns {number}
   */
  this.getHeightmapWidth = function () {
    return this.data.width;
  };
  /**
   * Returns the Height of the Heightmap of the terrain.
   * @returns {*|number}
   */
  this.getHeightmapHeight = function () {
    return this.data.height;
  };
};


/**
 * @class This terrain should receive multiple insertLevel calls. It removes the old version
 * and replace it with the new data. It can be used for progressive loading.
 * Example: WCPSDemAlpha with progressive loading using the progressiveWCPSImageLoader.
 * @augments EarthServerGenericClient.AbstractTerrain
 * @param index - Index of the model using this terrain.
 * @constructor
 */
EarthServerGenericClient.ProgressiveTerrain = function (index) {
  /**
   * General information about the amount of chunks needed to build the terrain.
   * @type {Object}
   */
  var chunkInfo;
  /**
   * Size of one chunk. Chunks at the borders can be smaller.
   * 256*256 (2^16) is the max size because of only 16 bit indices.
   * @type {number}
   */
  var chunkSize = 256;
  /**
   * The canvas that holds the received image.
   * @type {HTMLElement}
   */
  var canvasTexture;
  /**
   * Counter of the inserted levels.
   * @type {number}
   */
  var currentData = 0;

  /**
   * Insert one data level into the scene. The old elevation grid will be removed and one new build.
   * @param root - Dom Element to append the terrain to.
   * @param data - Received Data of the Server request.
   */
  this.insertLevel = function (root, data) {
    this.data = data;
    canvasTexture = this.createCanvas(data.texture, index);
    chunkInfo = this.calcNumberOfChunks(data.width, data.height, chunkSize);

    //Remove old Materials of the deleted children
    this.clearMaterials();

    for (var currentChunk = 0; currentChunk < chunkInfo.numChunks; currentChunk++) {
      try {
        //Build all necessary information and values to create a chunk
        var info = this.createChunkInfo(index, chunkSize, chunkInfo, currentChunk, data.width, data.height);
        var hm = this.getHeightMap(info);
        var appearance = this.getAppearances("TerrainApp_" + index + "_" + currentData, 1, index, canvasTexture, data.transparency);

        var transform = document.createElement('Transform');
        transform.setAttribute("translation", info.xpos + " 0 " + info.ypos);
        transform.setAttribute("scale", "1.0 1.0 1.0");

        new ElevationGrid(transform, info, hm, appearance);

        root.appendChild(transform);

        //Delete vars avoid circular references
        info = null;
        hm = null;
        appearance = null;
        transform = null;
      }
      catch (error) {
        alert('Terrain::CreateNewChunk(): ' + error);
      }
    }
    currentData++;
    canvasTexture = null;
    chunkInfo = null;

    EarthServerGenericClient.MainScene.reportProgress(index);
  };
};
EarthServerGenericClient.ProgressiveTerrain.inheritsFrom(EarthServerGenericClient.AbstractTerrain);


/**
 * @class This terrain build up a LOD with 3 levels of the received data.
 * @param root - Dom Element to append the terrain to.
 * @param data - Received Data of the Server request.
 * @param index - Index of the model that uses this terrain.
 * @augments EarthServerGenericClient.AbstractTerrain
 * @constructor
 */
EarthServerGenericClient.LODTerrain = function (root, data, index) {
  this.materialNodes = [];//Stores the IDs of the materials to change the transparency.
  this.data = data;

  /**
   * Distance to change between full and 1/2 resolution.
   * @type {number}
   */
  var lodRange1 = 2000;
  /**
   * Distance to change between 1/2 and 1/4 resolution.
   * @type {number}
   */
  var lodRange2 = 10000;


  /**
   * The canvas that holds the received image.
   * @type {HTMLElement}
   */
  var canvasTexture = this.createCanvas(data.texture, index);
  /**
   * Size of one chunk. Chunks at the borders can be smaller.
   * We want to build 3 chunks for the LOD with different resolution but the same size on the screen.
   * With 253 values the length of the most detailed chunk is 252.
   * The second chunk has 127 values and the length of 126. With a scale of 2 it's back to the size of 252.
   * The third chunk has 64 values and the length if 63. With a scale of 4 it's also back to the size 252.
   * @type {number}
   */
  var chunkSize = 253;
  /**
   * General information about the number of chunks needed to build the terrain.
   * @type {number}
   */
  var chunkInfo = this.calcNumberOfChunks(data.width, data.height, chunkSize);

  /**
   * Builds the terrain and appends into the scene.
   */
  this.createTerrain = function () {
    for (var currentChunk = 0; currentChunk < chunkInfo.numChunks; currentChunk++) {
      try {
        //Build all necessary information and values to create a chunk
        var info = this.createChunkInfo(index, chunkSize, chunkInfo, currentChunk, data.width, data.height);
        var hm = this.getHeightMap(info);
        var appearance = this.getAppearances("TerrainApp_" + index, 3, index, canvasTexture, data.transparency);

        var transform = document.createElement('Transform');
        transform.setAttribute("translation", info.xpos + " 0 " + info.ypos);
        transform.setAttribute("scale", "1.0 1.0 1.0");

        var lodNode = document.createElement('LOD');
        lodNode.setAttribute("Range", lodRange1 + ',' + lodRange2);
        lodNode.setAttribute("id", 'lod' + info.ID);

        new ElevationGrid(lodNode, info, hm, appearance);
        transform.appendChild(lodNode);
        root.appendChild(transform);

        //Delete vars avoid circular references
        info = null;
        hm = null;
        appearance = null;
        transform = null;
        lodNode = null;
      }
      catch (error) {
        alert('Terrain::CreateNewChunk(): ' + error);
      }
    }
    canvasTexture = null;
    chunkInfo = null;

    EarthServerGenericClient.MainScene.reportProgress(index);
  };
};
EarthServerGenericClient.LODTerrain.inheritsFrom(EarthServerGenericClient.AbstractTerrain);//Namespace
var EarthServerGenericClient = EarthServerGenericClient || {};

/**
 * @class Generic Server Response Data object. All requests store the response in an instance of this object.
 * One instance can be given as parameter for different requests if all requests writes different fields.
 * Example: One WMS request for the texture and one WCS request for the heightmap.
 */
EarthServerGenericClient.ServerResponseData = function () {
  this.heightmap = null;          //Heightmap
  this.heightmapUrl = "";         //If available, you can use the link as alternative.
  this.texture = new Image();     //Texture as image object
  this.texture.crossOrigin = '';  //Enable Texture to be edited (for alpha values for example)
  this.textureUrl = "";           //If available, you can use the link as alternative.
  this.width = 0;                 //Heightmap width
  this.height = 0;                //Heightmap height
  //The information about the heightmap are used to position a module correctly in the fishtank.
  //The minimum value as offset and the difference between minimum and maximum for scaling.
  this.minHMvalue = Number.MAX_VALUE;//Lowest value in the heightmap
  this.maxHMvalue = -Number.MAX_VALUE;//Highest value in the heigtmap
  this.averageHMvalue = 0;        //Average value of the heightmap

  /**
   * Validates if the response full successfully: Was an image and a height map received?
   * @returns {boolean} - True if both image and heightmap are present, false if not.
   */
  this.validate = function () {
    //Texture
    if (this.texture === undefined) {
      return false;
    }
    if (this.texture.width <= 0 || this.texture.height <= 0) {
      return false;
    }

    //Heightmap
    if (this.heightmap === null) {
      return false;
    }
    if (this.width === null || this.height === null) {
      return false;
    }
    if (this.minHMvalue === Number.MAX_VALUE || this.maxHMvalue === -Number.MAX_VALUE) {
      return false;
    }

    //Everything OK
    return true;
  };
};

/**
 * Small helper to synchronise multiple request callbacks. After all callbacks to this helper
 * are received the ResponseData object with all response data is send to the module.
 * After each request is received a progress update is send to the module.
 * @param callback - Module which requests the data.
 * @param numberToCombine - Number of callbacks that shall be received.
 */
EarthServerGenericClient.combinedCallBack = function (callback, numberToCombine) {
  var counter = 0;
  this.name = "Combined Callback: " + callback.name;
  EarthServerGenericClient.MainScene.timeLogStart("Combine: " + callback.name);

  /**
   * @ignore
   * @param data - Server response data object
   */
  this.receiveData = function (data) {
    counter++;
    if (counter == numberToCombine) {
      EarthServerGenericClient.MainScene.timeLogEnd("Combine: " + callback.name);
      callback.receiveData(data);
    }
  }

};

/**
 * Requests a WMS image, stores it in the responseData and make the callback once it is loaded.
 * @param callback - Object to do the callback.
 * @param responseData - Instance of the ServerResponseData.
 * @param WMSurl - URL of the WMS service.
 * @param WMScoverID - Coverage/Layer ID.
 * @param WMSCRS - The Coordinate Reference System. (Should be like: "crs=1")
 * @param WMSImageFormat - The image format that should be returned.
 * @param BoundingBox - The bounding box of the image.
 * @param WMSVersion - WMS Version that should be used.
 * @param width - Width of the response image.
 * @param height - Height of the response image.
 */
EarthServerGenericClient.getCoverageWMS = function (callback, responseData, WMSurl, WMScoverID, WMSCRS, WMSImageFormat, BoundingBox, WMSVersion, width, height) {
  responseData.textureUrl = WMSurl + "?service=WMS&version=" + WMSVersion + "&request=Getmap&layers=" + WMScoverID;
  responseData.textureUrl += "&" + WMSCRS + "&format=image/" + WMSImageFormat;
  responseData.textureUrl += "&bbox=" + BoundingBox.minLatitude + "," + BoundingBox.minLongitude + "," + BoundingBox.maxLatitude + "," + BoundingBox.maxLongitude;
  responseData.textureUrl += "&width=" + width + "&height=" + height;

  responseData.texture.onload = function () {
    callback.receiveData(responseData);
  };
  responseData.texture.onerror = function () {
    x3dom.debug.logInfo("Could not load Image.");
    callback.receiveData(responseData);
  };
  responseData.texture.src = responseData.textureUrl;

};

/**
 * Starts a WCPS query and stores the received image in the responseData.
 * If a dem is encoded in the alpha channel it will be extracted and also stored. Set DemInAlpha Flag in this case.
 * @param callback - Object to do the callback.
 * @param responseData - Instance of the ServerResponseData.
 * @param url - URL of the WCPS service.
 * @param query - The WCPS query.
 * @param DemInAlpha - Flag if a dem is encoded in the alpha channel.
 */
EarthServerGenericClient.getWCPSImage = function (callback, responseData, url, query, DemInAlpha) {
  try {
    responseData.texture.onload = function () {
      EarthServerGenericClient.MainScene.timeLogEnd("WCPS: " + callback.name);
      if (DemInAlpha) {
        responseData.heightmapUrl = responseData.texture.src;

        var canvas = document.createElement('canvas');
        canvas.width = responseData.texture.width;
        canvas.height = responseData.texture.height;
        //console.log("Image: " + responseData.texture.width +"x"+ responseData.texture.height);
        var context = canvas.getContext('2d');
        context.drawImage(responseData.texture, 0, 0);

        var hm = new Array(canvas.width);
        for (var k = 0; k < canvas.width; k++) {
          hm[k] = new Array(canvas.height);
        }

        responseData.width = hm.length;
        responseData.height = hm[0].length;

        var imageData = context.getImageData(0, 0, canvas.width, canvas.height);
        var total = 0;
        for (var i = 3; i < imageData.data.length; i += 4) {
          var index = i / 4;
          hm[parseInt(index % hm.length)][parseInt(index / hm.length)] = imageData.data[i];

          if (responseData.minHMvalue > imageData.data[i]) {
            responseData.minHMvalue = imageData.data[i]
          }
          if (responseData.maxHMvalue < imageData.data[i]) {
            responseData.maxHMvalue = imageData.data[i]
          }
          total = total + parseFloat(imageData.data[i]);

        }
        responseData.averageHMvalue = parseFloat(total / imageData.data.length);
        responseData.heightmap = hm;
      }

      x3dom.debug.logInfo("Server request done.");
      context = null;
      canvas = null;
      callback.receiveData(responseData);
    };
    responseData.texture.onerror = function () {
      x3dom.debug.logInfo("ServerRequest::wcpsRequest(): Could not load Image from url " + url + "! Aborted!");
      callback.receiveData(responseData);
    };

    responseData.textureUrl = url + "?query=" + encodeURI(query);
    EarthServerGenericClient.MainScene.timeLogStart("WCPS: " + callback.name);
    responseData.texture.src = responseData.textureUrl;
  }
  catch (error) {
    x3dom.debug.logInfo('ServerRequest::getWCPSImage(): ' + error);
    callback.receiveData(responseData);
  }
};

/**
 * Requests a WCS coverage and stores is the heightmap field of the responseData.
 * @param callback - Object to do the callback.
 * @param responseData - Instance of the ServerResponseData.
 * @param WCSurl - URl of the WCS service.
 * @param WCScoverID - ID of the coverage.
 * @param WCSBoundingBox - Bounding Box of the area.
 * @param WCSVersion - Version of used WCS service.
 */
EarthServerGenericClient.getCoverageWCS = function (callback, responseData, WCSurl, WCScoverID, WCSBoundingBox, WCSVersion) {
  var request = 'service=WCS&Request=GetCoverage&version=' + WCSVersion + '&CoverageId=' + WCScoverID;
  request += '&subsetx=x(' + WCSBoundingBox.minLatitude + ',' + WCSBoundingBox.maxLatitude + ')&subsety=y(' + WCSBoundingBox.minLongitude + ',' + WCSBoundingBox.maxLongitude + ')';

  EarthServerGenericClient.MainScene.timeLogStart("WCS Coverage: " + callback.name);

  $.ajax(
    {
      url     : WCSurl,
      type    : 'GET',
      dataType: 'XML',
      data    : request,
      success : function (receivedData) {
        EarthServerGenericClient.MainScene.timeLogEnd("WCS Coverage: " + callback.name);
        var Grid = $(receivedData).find('GridEnvelope');
        var low = $(Grid).find('low').text().split(" ");
        var high = $(Grid).find('high').text().split(" ");

        var sizeX = high[0] - low[0] + 1;
        var sizeY = high[1] - low[1] + 1;

        if (sizeX <= 0 || sizeY <= 0) {
          throw "getCoverageWCS: " + WCSurl + "/" + WCScoverID + ": Invalid grid size (" + sizeX + "," + sizeY + ")";
        }

        responseData.height = sizeX;
        responseData.width = sizeY;

        var hm = new Array(sizeX);
        for (var index = 0; index < hm.length; index++) {
          hm[index] = new Array(sizeY);
        }

        var DataBlocks = $(receivedData).find('DataBlock');
        DataBlocks.each(function () {
          var tuples = $(this).find("tupleList").text().split('},');
          for (var i = 0; i < tuples.length; i++) {
            var tmp = tuples[i].substr(1);
            var valuesList = tmp.split(",");

            for (var k = 0; k < valuesList.length; k++) {
              tmp = parseFloat(valuesList[k]);

              hm[parseInt(k / (sizeX))][parseInt(k % (sizeX))] = tmp;

              if (responseData.maxHMvalue < tmp) {
                responseData.maxHMvalue = parseInt(tmp);
              }
              if (responseData.minHMvalue > tmp) {
                responseData.minHMvalue = parseInt(tmp);
              }
            }
          }
          if (responseData.minHMvalue != 0 && responseData.maxHMvalue != 0) {
            responseData.averageHMvalue = (responseData.minHMvalue + responseData.maxHMvalue) / 2;
          }
          tuples = null;
        });
        DataBlocks = null;
        responseData.heightmap = hm;
        callback.receiveData(responseData);
      },
      error   : function (xhr, ajaxOptions, thrownError) {
        EarthServerGenericClient.MainScene.timeLogEnd("WCS Coverage: " + callback.name);
        x3dom.debug.logInfo('\t' + xhr.status + " " + ajaxOptions + " " + thrownError);
      }
    }
  );
};

/**
 * Requests one image via WCSPS. It is assumed that the image has a dem encoded in the alpha channel.
 * If not the terrain is flat.
 * @param callback - Module that requests the image.
 * @param WCPSurl - URL of the WCPS service.
 * @param WCPSquery - The WCPS query.
 */
EarthServerGenericClient.requestWCPSImageAlphaDem = function (callback, WCPSurl, WCPSquery) {
  var responseData = new EarthServerGenericClient.ServerResponseData();
  EarthServerGenericClient.getWCPSImage(callback, responseData, WCPSurl, WCPSquery, true);
};

/**
 * The progressive WCPS loader initiate multiple queries consecutively. As soon as one response is received the
 * next query is executed. Every response is given to the given callback.
 * Note: The WCPS loader starts with the last query in the array (LIFO).
 * @param callback - Module that requests the WCPS images.
 * @param WCPSurl - URL of the WCPS service.
 * @param WCPSqueries - Array of WCPS queries. (LIFO)
 * @param DemInAlpha - Flag if a dem is encoded in the alpha channel.
 */
EarthServerGenericClient.progressiveWCPSImageLoader = function (callback, WCPSurl, WCPSqueries, DemInAlpha) {
  var which = WCPSqueries.length - 1;
  //We need one responseData for every query in WCPSqueries
  var responseData = [];
  //For time logging.
  this.name = "Progressive WCPS Loader: " + callback.name;

  for (var i = 0; i < WCPSqueries.length; i++) {
    responseData[i] = new EarthServerGenericClient.ServerResponseData();
  }

  /**
   * @ignore
   * @param which - index of the request to make.
   */
  this.makeRequest = function (which) {
    if (which >= 0) {
      EarthServerGenericClient.MainScene.timeLogStart("Progressive WCPS: " + WCPSurl + "_Query_" + which);
      EarthServerGenericClient.getWCPSImage(this, responseData[which], WCPSurl, WCPSqueries[which], DemInAlpha);
    }
    else {
      responseData = null;
    }
  };
  /**
   * @ignore
   * @param data - Server response data object
   */
  this.receiveData = function (data) {
    EarthServerGenericClient.MainScene.timeLogEnd("Progressive WCPS: " + WCPSurl + "_Query_" + which);
    which--;
    this.makeRequest(which);
    callback.receiveData(data);
  };
  this.makeRequest(which);
};

/**
 * Requests an image via WCPS and a dem via WCS.
 * @param callback - Module requesting this data.
 * @param WCPSurl - URL of the WCPS service.
 * @param WCPSquery - WCPS Query for the image.
 * @param WCSurl - URL of the WCS service.
 * @param WCScoverID - Coverage ID for the WCS height data.
 * @param WCSBoundingBox - Bounding box of the area used in WCS.
 * @param WCSVersion - Version of the used WCS.
 */
EarthServerGenericClient.requestWCPSImageWCSDem = function (callback, WCPSurl, WCPSquery, WCSurl, WCScoverID, WCSBoundingBox, WCSVersion) {
  var responseData = new EarthServerGenericClient.ServerResponseData();
  var combine = new EarthServerGenericClient.combinedCallBack(callback, 2);

  EarthServerGenericClient.getWCPSImage(combine, responseData, WCPSurl, WCPSquery, false);
  EarthServerGenericClient.getCoverageWCS(combine, responseData, WCSurl, WCScoverID, WCSBoundingBox, WCSVersion);
};

/**
 * Requests an image via WMS and a dem via WCS.
 * @param callback - Module requesting this data.
 * @param BoundingBox - Bounding box of the area, used in both WMS and WCS requests.
 * @param ResX - Width of the response image via WMS.
 * @param ResY - Height of the response image via WMS.
 * @param WMSurl - URL of the WMS service.
 * @param WMScoverID - Layer ID used in WMS.
 * @param WMSversion - Version of the WMS service.
 * @param WMSCRS - The Coordinate Reference System. (Should be like: "crs=1")
 * @param WMSImageFormat - Image format for the WMS response.
 * @param WCSurl - URL of the WCS service.
 * @param WCScoverID - Coverage ID used in WCS.
 * @param WCSVersion - Version of the WCS service.
 */
EarthServerGenericClient.requestWMSImageWCSDem = function (callback, BoundingBox, ResX, ResY, WMSurl, WMScoverID, WMSversion, WMSCRS, WMSImageFormat, WCSurl, WCScoverID, WCSVersion) {
  var responseData = new EarthServerGenericClient.ServerResponseData();
  var combine = new EarthServerGenericClient.combinedCallBack(callback, 2);

  EarthServerGenericClient.getCoverageWMS(combine, responseData, WMSurl, WMScoverID, WMSCRS, WMSImageFormat, BoundingBox, WMSversion, ResX, ResY);
  EarthServerGenericClient.getCoverageWCS(combine, responseData, WCSurl, WCScoverID, BoundingBox, WCSVersion);
};//Namespace
var EarthServerGenericClient = EarthServerGenericClient || {};

/**
 * @class Scene Model: WCPS Image with DEM in Alpha Channel
 * 1 URL for the service, 2 Coverage names for the image and dem.
 * @augments EarthServerGenericClient.AbstractSceneModel
 */
EarthServerGenericClient.Model_WCPSDemAlpha = function () {
  this.setDefaults();
  this.name = "WCPS Image with DEM in alpha channel.";
  /**
   * Determines if progressive or complete loading of the model is used.
   * @default false
   * @type {Boolean}
   */
  this.progressiveLoading = false;

  /**
   * The custom or default WCPS Queries. The array contains either one element for complete loading
   * or multiple (3) queries for progressive loading of the model.
   * @type {Array}
   */
  this.WCPSQuery = [];
};
EarthServerGenericClient.Model_WCPSDemAlpha.inheritsFrom(EarthServerGenericClient.AbstractSceneModel);
/**
 * Enables/Disables the progressive loading of the model.
 * @param value - True or False
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.setProgressiveLoading = function (value) {
  this.progressiveLoading = value;

  //Progressive Loading creates 3 requests while normal loading 1
  if (this.progressiveLoading) {
    this.requests = 3;
  }
  else {
    this.requests = 1;
  }
};
/**
 * Sets the URL for the service.
 * @param url
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.setURL = function (url) {
  /**
   * URL for the WCPS service.
   * @type {String}
   */
  this.URLWCPS = String(url);
};
/**
 * Sets both coverage names.
 * @param coverageImage - Coverage name for the image data set.
 * @param coverageDem   - Coverage name for the dem data set.
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.setCoverages = function (coverageImage, coverageDem) {
  /**
   * Name of the image coverage.
   * @type {String}
   */
  this.coverageImage = String(coverageImage);
  /**
   * name of the dem coverage.
   * @type {String}
   */
  this.coverageDEM = String(coverageDem);
};
/**
 * Sets a specific querystring for the RED channel of the WCPS query.
 * All red,blue,green and alpha has to be set, otherwise the standard query will be used.
 * @param querystring - the querystring. Use $CI (coverageImage), $CD (coverageDEM),
 * $MINX,$MINY,$MAXX,$MAXY(AoI) and $RESX,ResZ (Resolution) for automatic replacement.
 * Examples: $CI.red , x($MINX:$MINY)
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.setWCPSForChannelRED = function (querystring) {
  this.WCPSQuery[0] = querystring;
};
/**
 * Sets a specific querystring for the GREEN channel of the WCPS query.
 * All red,blue,green and alpha has to be set, otherwise the standard query will be used.
 * @param querystring - the querystring. Use $CI (coverageImage), $CD (coverageDEM),
 * $MINX,$MINY,$MAXX,$MAXY(AoI) and $RESX,ResZ (Resolution) for automatic replacement.
 * Examples: $CI.red , x($MINX:$MINY)
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.setWCPSForChannelGREEN = function (querystring) {
  this.WCPSQuery[1] = querystring;
};
/**
 * Sets a specific querystring for the BLUE channel of the WCPS query.
 * All red,blue,green and alpha has to be set, otherwise the standard query will be used.
 * @param querystring - the querystring. Use $CI (coverageImage), $CD (coverageDEM),
 * $MINX,$MINY,$MAXX,$MAXY(AoI) and $RESX,ResZ (Resolution) for automatic replacement.
 * Examples: $CI.red , x($MINX:$MINY)
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.setWCPSForChannelBLUE = function (querystring) {
  this.WCPSQuery[2] = querystring;
};
/**
 * Sets a specific querystring for the ALPHA channel of the WCPS query.
 * All red,blue,green and alpha has to be set, otherwise the standard query will be used.
 * @param querystring - the querystring. Use $CI (coverageImage), $CD (coverageDEM),
 * $MINX,$MINY,$MAXX,$MAXY(AoI) and $RESX,ResZ (Resolution) for automatic replacement.
 * Examples: $CI.red , x($MINX:$MINY)
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.setWCPSForChannelALPHA = function (querystring) {
  this.WCPSQuery[3] = querystring;
};

/**
 * Sets the Coordinate Reference System.
 * @param value - eg. "http://www.opengis.net/def/crs/EPSG/0/27700"
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.setCoordinateReferenceSystem = function (value) {
  this.CRS = value;
};

/**
 * Creates the x3d geometry and appends it to the given root node. This is done automatically by the SceneManager.
 * @param root - X3D node to append the model.
 * @param cubeSizeX - Size of the fishtank/cube on the x-axis.
 * @param cubeSizeY - Size of the fishtank/cube on the y-axis.
 * @param cubeSizeZ - Size of the fishtank/cube on the z-axis.
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.createModel = function (root, cubeSizeX, cubeSizeY, cubeSizeZ) {
  if (root === undefined)
    alert("root is not defined");

  EarthServerGenericClient.MainScene.timeLogStart("Create Model " + this.name);

  this.cubeSizeX = cubeSizeX;
  this.cubeSizeY = cubeSizeY;
  this.cubeSizeZ = cubeSizeZ;

  this.root = root;

  //Create Placeholder
  this.createPlaceHolder();

  //1: Check if mandatory values are set
  if (this.coverageImage === undefined || this.coverageDEM === undefined || this.URLWCPS === undefined || this.CRS === undefined
    || this.minx === undefined || this.miny === undefined || this.maxx === undefined || this.maxy === undefined) {
    alert("Not all mandatory values are set. WCPSDemAlpha: " + this.name);
    console.log(this);
    return;
  }

  //2: create wcps query/queries
  //Either the user query if all query strings are set. Or standard wcps query if wcps channels are not set.
  //Build one query for complete loading and multiple queries for progressive loading

  //IF something is not defined use standard query.
  if (this.WCPSQuery[0] === undefined || this.WCPSQuery[1] === undefined || this.WCPSQuery[2] === undefined || this.WCPSQuery[3] === undefined) {
    for (var i = 0; i < this.requests; i++) {
      var currentXRes = parseInt(this.XResolution / Math.pow(2, i));
      var currentZRes = parseInt(this.ZResolution / Math.pow(2, i));
      this.WCPSQuery[i] = "for i in (" + this.coverageImage + "), dtm in (" + this.coverageDEM + ") return encode ( { ";
      this.WCPSQuery[i] += 'red: scale(trim(i.red, {x:"' + this.CRS + '"(' + this.minx + ":" + this.maxx + '), y:"' + this.CRS + '"(' + this.miny + ":" + this.maxy + ') }), {x:"CRS:1"(0:' + currentXRes + '), y:"CRS:1"(0:' + currentZRes + ")}, {}); ";
      this.WCPSQuery[i] += 'green: scale(trim(i.green, {x:"' + this.CRS + '"(' + this.minx + ":" + this.maxx + '), y:"' + this.CRS + '"(' + this.miny + ":" + this.maxy + ') }), {x:"CRS:1"(0:' + currentXRes + '), y:"CRS:1"(0:' + currentZRes + ")}, {}); ";
      this.WCPSQuery[i] += 'blue: scale(trim(i.blue, {x:"' + this.CRS + '"(' + this.minx + ":" + this.maxx + '), y:"' + this.CRS + '"(' + this.miny + ":" + this.maxy + ') }), {x:"CRS:1"(0:' + currentXRes + '), y:"CRS:1"(0:' + currentZRes + ")}, {});";
      this.WCPSQuery[i] += 'alpha: (char) (((scale(trim(dtm , {x:"' + this.CRS + '"(' + this.minx + ":" + this.maxx + '), y:"' + this.CRS + '"(' + this.miny + ":" + this.maxy + ') }), {x:"CRS:1"(0:' + currentXRes + '), y:"CRS:1"(0:' + currentZRes + ")}, {})) / 1349) * 255)";
      this.WCPSQuery[i] += '}, "' + this.imageFormat + '" )';
    }
  }
  else //ALL set so use custom query
  {
    //Create multiple queries if progressive loading is set or one if not.
    for (var j = 0; j < this.requests; j++) {
      //Replace $ symbols with the actual values
      var tmpString = [];
      for (i = 0; i < 4; i++) {
        tmpString[i] = this.WCPSQuery[i].replace("$CI", "image");
        tmpString[i] = tmpString[i].replace("$CD", "dtm");
        tmpString[i] = tmpString[i].replace("$MINX", this.minx);
        tmpString[i] = tmpString[i].replace("$MINY", this.miny);
        tmpString[i] = tmpString[i].replace("$MAXX", this.maxx);
        tmpString[i] = tmpString[i].replace("$MAXY", this.maxy);
        tmpString[i] = tmpString[i].replace("$CRS", '"' + this.CRS + '"');
        tmpString[i] = tmpString[i].replace("$CRS", '"' + this.CRS + '"');
        tmpString[i] = tmpString[i].replace("$RESX", parseInt(this.XResolution / Math.pow(2, j)));
        tmpString[i] = tmpString[i].replace("$RESZ", parseInt(this.ZResolution / Math.pow(2, j)));
      }
      this.WCPSQuery[j] = "for image in (" + this.coverageImage + "), dtm in (" + this.coverageDEM + ") return encode ( { ";
      this.WCPSQuery[j] += "red: " + tmpString[0] + " ";
      this.WCPSQuery[j] += "green: " + tmpString[1] + " ";
      this.WCPSQuery[j] += "blue: " + tmpString[2] + " ";
      this.WCPSQuery[j] += "alpha: " + tmpString[3];
      this.WCPSQuery[j] += '}, "' + this.imageFormat + '" )';
    }
  }

  //3: Make ServerRequest and receive data.
  if (!this.progressiveLoading) {
    EarthServerGenericClient.requestWCPSImageAlphaDem(this, this.URLWCPS, this.WCPSQuery[0]);
  }
  else {
    EarthServerGenericClient.progressiveWCPSImageLoader(this, this.URLWCPS, this.WCPSQuery, true);
  }
};
/**
 * This is a callback method as soon as the ServerRequest in createModel() has received it's data.
 * This is done automatically.
 * @param data - Received data from the ServerRequest.
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.receiveData = function (data) {
  if (this.checkReceivedData(data)) {
    //If progressive loading is enabled this function is called multiple times.
    //The lower resolution version shall be removed and replaced with the new one.
    //So the old transformNode will be removed and a new one created.
    if (this.transformNode !== undefined) {
      this.root.removeChild(this.transformNode);
    }

    //In the first receiveData call remove the placeholder.
    this.removePlaceHolder();

    var YResolution = (parseFloat(data.maxHMvalue) - parseFloat(data.minHMvalue) );
    this.transformNode = this.createTransform(data.width, YResolution, data.height, parseFloat(data.minHMvalue));
    this.root.appendChild(this.transformNode);

    //Set transparency
    data.transparency = this.transparency;

    //Create Terrain out of the received data
    if (!this.progressiveLoading) {
      EarthServerGenericClient.MainScene.timeLogStart("Create Terrain " + this.name);
      this.terrain = new EarthServerGenericClient.LODTerrain(this.transformNode, data, this.index);
      this.terrain.createTerrain();
      EarthServerGenericClient.MainScene.timeLogEnd("Create Terrain " + this.name);
      EarthServerGenericClient.MainScene.timeLogEnd("Create Model " + this.name);
    }
    else {
      //Check if terrain is already created. Create it in the first function call.
      if (this.terrain === undefined) {
        this.terrain = new EarthServerGenericClient.ProgressiveTerrain(this.index);
      }

      //Add new data (with higher resolution) to the terrain
      EarthServerGenericClient.MainScene.timeLogStart("Create Terrain " + this.name);
      this.terrain.insertLevel(this.transformNode, data);
      EarthServerGenericClient.MainScene.timeLogEnd("Create Terrain " + this.name);

      if (this.receivedDataCount === this.requests) {
        EarthServerGenericClient.MainScene.timeLogEnd("Create Model " + this.name);
      }
    }

    //Delete transformNode when the last response call is done.
    //Until that the pointer is needed to delete the old terrain just before the new terrain is build.
    if (this.receivedDataCount === this.requests) {
      this.transformNode = null;
    }
  }
};

/**
 * Every Scene Model creates it's own specific UI elements. This function is called automatically by the SceneManager.
 * @param element - The element where to append the specific UI elements for this model.
 */
EarthServerGenericClient.Model_WCPSDemAlpha.prototype.setSpecificElement = function (element) {
  EarthServerGenericClient.appendElevationSlider(element, this.index);
};//Namespace
var EarthServerGenericClient = EarthServerGenericClient || {};

/**
 * @class Scene Model: WCPS Image with DEM from WCS Query
 * 2 URLs for the service, 2 Coverage names for the image and dem.
 * @augments EarthServerGenericClient.AbstractSceneModel
 */
EarthServerGenericClient.Model_WCPSDemWCS = function () {
  this.setDefaults();
  this.name = "WCPS Image with DEM from WCS Query.";
  /**
   * WCS version for the query.
   * @default "2.0.0"
   * @type {String}
   */
  this.WCSVersion = "2.0.0";
};
EarthServerGenericClient.Model_WCPSDemWCS.inheritsFrom(EarthServerGenericClient.AbstractSceneModel);
/**
 * Sets the url for both the WCPS and WCS Queries.
 * @param wcpsurl - Service URL for the WCPS Request
 * @param demurl  - Service URL for the WCS Request
 */
EarthServerGenericClient.Model_WCPSDemWCS.prototype.setURLs = function (wcpsurl, demurl) {
  /**
   * URL for the WCPS service.
   * @type {String}
   */
  this.URLWCPS = String(wcpsurl);
  /**
   * URL for the WCS service.
   * @type {String}
   */
  this.URLDEM = String(demurl);
};
/**
 * Sets both coveragenames
 * @param coverageImage - Coverage name for the image dataset.
 * @param coverageDem   - Coverage name for the dem dataset.
 */
EarthServerGenericClient.Model_WCPSDemWCS.prototype.setCoverages = function (coverageImage, coverageDem) {
  /**
   * Name of the image coverage.
   * @type {String}
   */
  this.coverageImage = String(coverageImage);
  /**
   * Name if the dem coverage.
   * @type {String}
   */
  this.coverageDEM = String(coverageDem);
};
/**
 * Sets a complete custom querystring.
 * @param querystring - the querystring. Use $CI (coverageImage), $CD (coverageDEM),
 * $MINX,$MINY,$MAXX,$MAXY(AoI) and $RESX,ResZ (Resolution) for automatic replacement.
 * Examples: $CI.red , x($MINX:$MINY)
 */
EarthServerGenericClient.Model_WCPSDemWCS.prototype.setWCPSQuery = function (querystring) {
  /**
   * The custom query.
   * @type {String}
   */
  this.WCPSQuery = String(querystring);
};

/**
 * Sets the WCS Version for the WCS Query String. Default: "2.0.0"
 * @param version - String with WCS version number.
 */
EarthServerGenericClient.Model_WCPSDemWCS.prototype.setWCSVersion = function (version) {
  this.WCSVersion = String(version);
};

/**
 * Sets the Coordinate Reference System.
 * @param value - eg. "http://www.opengis.net/def/crs/EPSG/0/27700"
 */
EarthServerGenericClient.Model_WCPSDemWCS.prototype.setCoordinateReferenceSystem = function (value) {
  this.CRS = value;
};

/**
 * Creates the x3d geometry and appends it to the given root node. This is done automatically by the SceneManager.
 * @param root - X3D node to append the model.
 * @param cubeSizeX - Size of the fishtank/cube on the x-axis.
 * @param cubeSizeY - Size of the fishtank/cube on the y-axis.
 * @param cubeSizeZ - Size of the fishtank/cube on the z-axis.
 */
EarthServerGenericClient.Model_WCPSDemWCS.prototype.createModel = function (root, cubeSizeX, cubeSizeY, cubeSizeZ) {
  if (root === undefined)
    alert("root is not defined");

  EarthServerGenericClient.MainScene.timeLogStart("Create Model " + this.name);

  this.cubeSizeX = cubeSizeX;
  this.cubeSizeY = cubeSizeY;
  this.cubeSizeZ = cubeSizeZ;

  this.root = root;

  //Create Placeholder
  this.createPlaceHolder();

  //1: Check if mandatory values are set
  if (this.coverageImage === undefined || this.coverageDEM === undefined || this.URLWCPS === undefined || this.URLDEM === undefined
    || this.minx === undefined || this.miny === undefined || this.maxx === undefined || this.maxy === undefined) {
    alert("Not all mandatory values are set. WCPSDemWCS: " + this.name);
    console.log(this);
    return;
  }

  //2: create wcps query
  //If no query was defined use standard query.
  if (this.WCPSQuery === undefined) {
    this.WCPSQuery = "for i in (" + this.coverageImage + "), dtm in (" + this.coverageDEM + ") return encode ( { ";
    this.WCPSQuery += 'red: scale(trim(i.red, {x:"' + this.CRS + '"(' + this.minx + ":" + this.maxx + '), y:' + this.CRS + '"(' + this.miny + ":" + this.maxy + ') }), {x:"CRS:1"(0:' + this.XResolution + '), y:"CRS:1"(0:' + this.ZResolution + ")}, {}); ";
    this.WCPSQuery += 'green: scale(trim(i.green, {x:"' + this.CRS + '"(' + this.minx + ":" + this.maxx + '), y:' + this.CRS + '"(' + this.miny + ":" + this.maxy + ') }), {x:"CRS:1"(0:' + this.XResolution + '), y:"CRS:1"(0:' + this.ZResolution + ")}, {}); ";
    this.WCPSQuery += 'blue: scale(trim(i.blue, {x(:"' + this.CRS + '"(' + this.minx + ":" + this.maxx + '), y:' + this.CRS + '"(' + this.miny + ":" + this.maxy + ') }), {x:"CRS:1"(0:' + this.XResolution + '), y:"CRS:1"(0:' + this.ZResolution + ")}, {})";
    this.WCPSQuery += '}, "' + this.imageFormat + '" )';
  }
  else //A custom query was defined so use it
  {
    //Replace $ symbols with the actual values
    this.WCPSQuery = this.WCPSQuery.replace("$CI", this.coverageImage);
    this.WCPSQuery = this.WCPSQuery.replace("$MINX", this.minx);
    this.WCPSQuery = this.WCPSQuery.replace("$MINY", this.miny);
    this.WCPSQuery = this.WCPSQuery.replace("$MAXX", this.maxx);
    this.WCPSQuery = this.WCPSQuery.replace("$MAXY", this.maxy);
    this.WCPSQuery = this.WCPSQuery.replace("$CRS", '"' + this.CRS + '"');
    this.WCPSQuery = this.WCPSQuery.replace("$CRS", '"' + this.CRS + '"');
    this.WCPSQuery = this.WCPSQuery.replace("$RESX", this.XResolution);
    this.WCPSQuery = this.WCPSQuery.replace("$RESZ", this.ZResolution);
  }

  //3: Make ServerRequest and receive data.
  var bb = {
    minLongitude: this.miny,
    maxLongitude: this.maxy,
    minLatitude : this.minx,
    maxLatitude : this.maxx
  };
  EarthServerGenericClient.requestWCPSImageWCSDem(this, this.URLWCPS, this.WCPSQuery, this.URLDEM, this.coverageDEM, bb, this.WCSVersion);
};

/**
 * This is a callback method as soon as the ServerRequest in createModel() has received it's data.
 * This is done automatically.
 * @param data - Received data from the ServerRequest.
 */
EarthServerGenericClient.Model_WCPSDemWCS.prototype.receiveData = function (data) {
  if (this.checkReceivedData(data)) {
    //Remove the placeHolder
    this.removePlaceHolder();

    var YResolution = (parseFloat(data.maxHMvalue) - parseFloat(data.minHMvalue) );
    var transform = this.createTransform(data.width, YResolution, data.height, parseFloat(data.minHMvalue));
    this.root.appendChild(transform);

    //Set transparency
    data.transparency = this.transparency;
    //Create Terrain out of the received data
    EarthServerGenericClient.MainScene.timeLogStart("Create Terrain " + this.name);
    this.terrain = new EarthServerGenericClient.LODTerrain(transform, data, this.index);
    this.terrain.createTerrain();
    EarthServerGenericClient.MainScene.timeLogEnd("Create Terrain " + this.name);
    EarthServerGenericClient.MainScene.timeLogEnd("Create Model " + this.name);

    transform = null;
  }
};


/**
 * Every Scene Model creates it's own specific UI elements. This function is called automatically by the SceneManager.
 * @param element - The element where to append the specific UI elements for this model.
 */
EarthServerGenericClient.Model_WCPSDemWCS.prototype.setSpecificElement = function (element) {
  EarthServerGenericClient.appendElevationSlider(element, this.index);
};//Namespace
var EarthServerGenericClient = EarthServerGenericClient || {};

/**
 * @class Scene Model: WMS Image with DEM from WCS Query
 * 2 URLs for the service, 2 Coverage names for the image and dem.
 * @augments EarthServerGenericClient.AbstractSceneModel
 */
EarthServerGenericClient.Model_WMSDemWCS = function () {
  this.setDefaults();
  this.name = "WMS Image with DEM from WCS Query.";
  /**
   * WCS version for the query.
   * @default "2.0.0"
   * @type {String}
   */
  this.WCSVersion = "2.0.0";
  /**
   * WMS version for the query.
   * @default "1.3"
   * @type {String}
   */
  this.WMSVersion = "1.3";
};
EarthServerGenericClient.Model_WMSDemWCS.inheritsFrom(EarthServerGenericClient.AbstractSceneModel);
/**
 * Sets the url for both the WMS and WCS Queries.
 * @param WMSurl - Service URL for the WMS Request
 * @param demurl  - Service URL for the WCS Request
 */
EarthServerGenericClient.Model_WMSDemWCS.prototype.setURLs = function (WMSurl, demurl) {
  /**
   * URL for the WMS service.
   * @type {String}
   */
  this.URLWMS = String(WMSurl);
  /**
   * URL for the WCS service.
   * @type {String}
   */
  this.URLDEM = String(demurl);
};
/**
 * Sets both coverage names
 * @param coverageImage - Coverage name for the image data set.
 * @param coverageDem   - Coverage name for the dem data set.
 */
EarthServerGenericClient.Model_WMSDemWCS.prototype.setCoverages = function (coverageImage, coverageDem) {
  /**
   * Name of the image coverage.
   * @type {String}
   */
  this.coverageImage = String(coverageImage);
  /**
   * Name if the dem coverage.
   * @type {String}
   */
  this.coverageDEM = String(coverageDem);
};
/**
 * Sets the WCS Version for the WCS Query String. Default: "2.0.0"
 * @param version - String with WCS version number.
 */
EarthServerGenericClient.Model_WMSDemWCS.prototype.setWCSVersion = function (version) {
  this.WCSVersion = String(version);
};
/**
 * Sets the WMS Version for the WMS Query String. Default: "1.3"
 * @param version - String with WMS version number.
 */
EarthServerGenericClient.Model_WMSDemWCS.prototype.setWMSVersion = function (version) {
  this.WMSVersion = String(version);
};
/**
 * Sets the Coordinate Reference System.
 * @param System - eg. CRS,SRS
 * @param value - eg. EPSG:4326
 */
EarthServerGenericClient.Model_WMSDemWCS.prototype.setCoordinateReferenceSystem = function (System, value) {
  this.CRS = System + "=" + value;
};

/**
 * Creates the x3d geometry and appends it to the given root node. This is done automatically by the SceneManager.
 * @param root - X3D node to append the model.
 * @param cubeSizeX - Size of the fishtank/cube on the x-axis.
 * @param cubeSizeY - Size of the fishtank/cube on the y-axis.
 * @param cubeSizeZ - Size of the fishtank/cube on the z-axis.
 */
EarthServerGenericClient.Model_WMSDemWCS.prototype.createModel = function (root, cubeSizeX, cubeSizeY, cubeSizeZ) {
  if (root === undefined)
    alert("root is not defined");

  EarthServerGenericClient.MainScene.timeLogStart("Create Model " + this.name);

  this.cubeSizeX = cubeSizeX;
  this.cubeSizeY = cubeSizeY;
  this.cubeSizeZ = cubeSizeZ;

  this.root = root;

  //Create Placeholder
  this.createPlaceHolder();

  //1: Check if mandatory values are set
  if (this.coverageImage === undefined || this.coverageDEM === undefined || this.URLWMS === undefined || this.URLDEM === undefined
    || this.minx === undefined || this.miny === undefined || this.maxx === undefined || this.maxy === undefined || this.CRS === undefined) {
    alert("Not all mandatory values are set. WMSDemWCS: " + this.name);
    console.log(this);
    return;
  }

  //2: Make ServerRequest and receive data.
  var bb = {
    minLongitude: this.miny,
    maxLongitude: this.maxy,
    minLatitude : this.minx,
    maxLatitude : this.maxx
  };

  EarthServerGenericClient.requestWMSImageWCSDem(this, bb, this.XResolution, this.ZResolution,
    this.URLWMS, this.coverageImage, this.WMSVersion, this.CRS, this.imageFormat,
    this.URLDEM, this.coverageDEM, this.WCSVersion);
};

/**
 * This is a callback method as soon as the ServerRequest in createModel() has received it's data.
 * This is done automatically.
 * @param data - Received data from the ServerRequest.
 */
EarthServerGenericClient.Model_WMSDemWCS.prototype.receiveData = function (data) {
  if (this.checkReceivedData(data)) {
    //Remove the placeHolder
    this.removePlaceHolder();

    var YResolution = (parseFloat(data.maxHMvalue) - parseFloat(data.minHMvalue) );
    var transform = this.createTransform(data.width, YResolution, data.height, parseFloat(data.minHMvalue));
    this.root.appendChild(transform);

    //Set transparency
    data.transparency = this.transparency;
    //Create Terrain out of the received data
    EarthServerGenericClient.MainScene.timeLogStart("Create Terrain " + this.name);
    this.terrain = new EarthServerGenericClient.LODTerrain(transform, data, this.index);
    this.terrain.createTerrain();
    EarthServerGenericClient.MainScene.timeLogEnd("Create Terrain " + this.name);
    EarthServerGenericClient.MainScene.timeLogEnd("Create Model " + this.name);

    transform = null;
  }
};


/**
 * Every Scene Model creates it's own specific UI elements. This function is called automatically by the SceneManager.
 * @param element - The element where to append the specific UI elements for this model.
 */
EarthServerGenericClient.Model_WMSDemWCS.prototype.setSpecificElement = function (element) {
  EarthServerGenericClient.appendElevationSlider(element, this.index);
};//Namespace
var EarthServerGenericClient = EarthServerGenericClient || {};

/**
 * Creates the basic UI
 * @param domElementID - Dom element to append the UI to.
 */
EarthServerGenericClient.createBasicUI = function (domElementID) {
  var UI_DIV = document.getElementById(domElementID);
  if (!UI_DIV) {
    alert("Can't find DomElement for UI with ID " + domElementID);
    return;
  }

  //Create Divs for all scene models
  for (var i = 0; i < EarthServerGenericClient.MainScene.getModelCount(); i++) {
    var name = document.createElement("h3");
    name.innerHTML = EarthServerGenericClient.MainScene.getModelName(i);
    var div = document.createElement("div");
    //Set IDs
    name.setAttribute("id", "EarthServerGenericClient_ModelHeader_" + i);
    div.setAttribute("id", "EarthServerGenericClient_ModelDiv_" + i);

    UI_DIV.appendChild(name);
    UI_DIV.appendChild(div);

    EarthServerGenericClient.appendAlphaSlider(div, i);
    EarthServerGenericClient.MainScene.setSpecificElement(i, div);
    div = null;
    p = null;
  }

  //Create Div for the Cameras
  var Cam = document.createElement("h3");
  Cam.innerHTML = "Cameras";
  var cdiv = document.createElement("div");
  var cp = document.createElement("p");

  for (i = 0; i < EarthServerGenericClient.MainScene.getCameraDefCount(); i++) {
    var button = document.createElement('button');
    var cameraDef = EarthServerGenericClient.MainScene.getCameraDef(i);
    cameraDef = cameraDef.split(":");
    button.setAttribute("onclick", "EarthServerGenericClient.MainScene.setView('" + cameraDef[1] + "');return false;");
    button.innerHTML = cameraDef[0];

    cp.appendChild(button);
    button = null;
  }
  cdiv.appendChild(cp);
  UI_DIV.appendChild(Cam);
  UI_DIV.appendChild(cdiv);

  cdiv = null;
  cp = null;

  //Create Divs for a Light sources
  for (i = 0; i < EarthServerGenericClient.MainScene.getLightCount(); i++) {
    var lightHeader = document.createElement("h3");
    lightHeader.innerHTML = "Light " + i;
    var lightDiv = document.createElement("div");

    UI_DIV.appendChild(lightHeader);
    UI_DIV.appendChild(lightDiv);

    EarthServerGenericClient.appendXYZSlider(lightDiv, "Light" + i + "X", "X Translation", i, 0,
      -EarthServerGenericClient.MainScene.getCubeSizeX(), EarthServerGenericClient.MainScene.getCubeSizeX(), 0,
      EarthServerGenericClient.MainScene.updateLightPosition);

    EarthServerGenericClient.appendXYZSlider(lightDiv, "Light" + i + "Y", "Y Translation", i, 1,
      -EarthServerGenericClient.MainScene.getCubeSizeY(), EarthServerGenericClient.MainScene.getCubeSizeY(), 0,
      EarthServerGenericClient.MainScene.updateLightPosition);

    EarthServerGenericClient.appendXYZSlider(lightDiv, "Light" + i + "Z", "Z Translation", i, 2,
      -EarthServerGenericClient.MainScene.getCubeSizeZ(), EarthServerGenericClient.MainScene.getCubeSizeZ(), 0,
      EarthServerGenericClient.MainScene.updateLightPosition);

    EarthServerGenericClient.appendGenericSlider(lightDiv, "Light" + i + "R", "Radius", i, 0, 5000, 500,
      EarthServerGenericClient.MainScene.updateLightRadius);

    EarthServerGenericClient.appendGenericSlider(lightDiv, "Light" + i + "I", "Intensity", i, 0, 10, 2,
      EarthServerGenericClient.MainScene.updateLightIntensity);

    lightDiv = null;
    lightHeader = null;
  }

  //Create Div for the Annotations
  if (EarthServerGenericClient.MainScene.getAnnotationLayerCount()) {
    var Anno = document.createElement("h3");
    Anno.innerHTML = "Annotations";
    var adiv = document.createElement("div");

    for (i = 0; i < EarthServerGenericClient.MainScene.getAnnotationLayerCount(); i++) {
      var ap = document.createElement("p");

      var ALname = EarthServerGenericClient.MainScene.getAnnotationLayerName(i);
      ap.innerHTML = ALname + ": ";
      var checkbox = document.createElement("input");
      checkbox.setAttribute("type", "checkbox");
      checkbox.setAttribute("checked", "checked");
      checkbox.setAttribute("onchange", "EarthServerGenericClient.MainScene.drawAnnotationLayer('" + ALname + "',this.checked)");
      ap.appendChild(checkbox);
      //Build list with annotations in this layer
      var list = document.createElement("ul");
      var annotationTexts = EarthServerGenericClient.MainScene.getAnnotationLayerTexts(ALname);
      for (var k = 0; k < annotationTexts.length; k++) {
        var entry = document.createElement("li");
        entry.innerHTML = annotationTexts[k];
        list.appendChild(entry);
        entry = null;
      }

      ap.appendChild(list);
      adiv.appendChild(ap);
      ap = null;
      checkbox = null;
      list = null;
    }

    UI_DIV.appendChild(Anno);
    UI_DIV.appendChild(adiv);

    adiv = null;
    ap = null;
  }
  $("#" + domElementID).accordion({
    heightStyle: "content",
    collapsible: true
  });

  UI_DIV = null;
};

/**
 * Appends a axis slider to a UI element. Axis sliders call the callback function with an ID,axis and their value.
 * @param domElement - Append the slider to this dom element.
 * @param sliderID - Dom ID for this slider.
 * @param label - Label (displayed in the UI) for this slider
 * @param elementID - First parameter for the callback function. Change the element with this ID.
 * @param axis - Axis this slider should effect. 0:x 1:y 2:z
 * @param min - Minimum value of this slider.
 * @param max - Maximum value of this slider.
 * @param startValue - Start value of this slider.
 * @param callback - Callback function, every time the slider is moved this function will be called.
 */
EarthServerGenericClient.appendXYZSlider = function (domElement, sliderID, label, elementID, axis, min, max, startValue, callback) {
  var p = document.createElement("p");
  p.innerHTML = label;
  domElement.appendChild(p);

  var slider = document.createElement("div");
  slider.setAttribute("id", sliderID);
  domElement.appendChild(slider);

  var jsslider = new Rj.widget.HorizontalSlider("#" + sliderID, min, max);
  jsslider.setValue(startValue);
  jsslider.addListener(sliderID, "valuechanged", function(value){
    callback(elementID, axis, value);
  })
};

/**
 * Generic sliders are calling their callback function with an element ID and their value.
 * @param domElement - Append the slider to this dom element.
 * @param sliderID - Dom ID for this slider.
 * @param label - Label (displayed in the UI) for this slider
 * @param elementID - First parameter for the callback function. Change the element with this ID.
 * @param min - Minimum value of this slider.
 * @param max - Maximum value of this slider.
 * @param startValue - Start value of this slider.
 * @param callback - Callback function, every time the slider is moved this function will be called.
 */
EarthServerGenericClient.appendGenericSlider = function (domElement, sliderID, label, elementID, min, max, startValue, callback) {
  var p = document.createElement("p");
  p.innerHTML = label;
  domElement.appendChild(p);

  var slider = document.createElement("div");
  slider.setAttribute("id", sliderID);
  domElement.appendChild(slider);

  var jsslider = new Rj.widget.HorizontalSlider("#" + sliderID, min, max);
  jsslider.setValue(startValue);
  jsslider.addListener(sliderID, "valuechanged", function(value){
    callback(elementID, value);
  })

};

/**
 * Special slider for setting the transparency of scene models.
 * @param domElement - Append the slider to this dom element.
 * @param moduleNumber - Index of the scene model.
 */
EarthServerGenericClient.appendAlphaSlider = function (domElement, moduleNumber) {
  //AlphaChannel
  var ap = document.createElement("p");
  ap.setAttribute("id", "EarthServerGenericClient_SliderCell_a_" + moduleNumber);
  ap.innerHTML = "Transparency: ";
  domElement.appendChild(ap);

  //jQueryUI Slider
  var Aslider = document.createElement("div");
  Aslider.setAttribute("id", "aSlider_" + moduleNumber);
  domElement.appendChild(Aslider);

  this.transparencySlider = new Rj.widget.HorizontalSlider("#aSlider_" + moduleNumber, 0, 100);
  this.transparencySlider.setValue(EarthServerGenericClient.MainScene.getModelTransparency(moduleNumber) * 100);
  this.transparencySlider.addListener("earthServerA" + moduleNumber, "valuechanged", function (value) {
    EarthServerGenericClient.MainScene.updateTransparency(moduleNumber, parseFloat(value / 100));
  })

};

/**
 * Special slider for setting the elevation of scene models.
 * @param domElement - Append the slider to this dom element.
 * @param moduleNumber - Index of the scene model.
 */
EarthServerGenericClient.appendElevationSlider = function (domElement, moduleNumber) {

  var ep = document.createElement("p");
  ep.setAttribute("id", "EarthServerGenericClient_SliderCell_e_" + moduleNumber);
  ep.innerHTML = "Elevation: ";
  domElement.appendChild(ep);

  //jQueryUI Slider
  var Eslider = document.createElement("div");
  Eslider.setAttribute("id", "eSlider_" + moduleNumber);
  domElement.appendChild(Eslider);

  this.elevationSlider = new Rj.widget.HorizontalSlider("#eSlider_" + moduleNumber, 0, 100);
  //this.elevationSlider.setValue(10);
  this.elevationSlider.addListener("earthServerE" + moduleNumber, "valuechanged", function (value) {
    EarthServerGenericClient.MainScene.updateElevation(moduleNumber, value);
  })

};

/**
 * @class The default progress bar to display the progress in loading and creating the scene models.
 * @param DivID
 */
EarthServerGenericClient.createProgressBar = function (DivID) {
  $("#" + DivID).progressbar({ value: 0, max: 100 });
  $("#" + DivID).on("progressbarcomplete", function (event, ui) {
    $("#" + DivID).toggle("blind");
  });

  /**
   * Updates the value in the progress bar.
   * @param value - New value
   */
  this.updateValue = function (value) {
    $("#" + DivID).progressbar("option", "value", value);
  };
};

//Namespace
var EarthServerGenericClient = EarthServerGenericClient || {};

/**
 * @class Annotation Layer to create multiple Annotations with the same style who belong together.
 * @param Name - Name of the Layer. To be displayed and to add annotations to it.
 * @param root - X3dom element to append the annotations.
 * @param fontSize - Font size of the annotations.
 * @param fontColor - Font color of the annotations
 * @param fontHover - The annotations hovers above the marker by this value.
 * @param markerSize - Size of the annotations marker.
 * @param markerColor - Color of the annotations marker.
 * @constructor
 */
EarthServerGenericClient.AnnotationLayer = function (Name, root, fontSize, fontColor, fontHover, markerSize, markerColor) {

  this.name = Name;   //Name of this layer
  var annotationTransforms = []; //Array with all transform to switch rendering
  var annotations = [];   //The text of the annotations (displayed in the UI)


  /**
   * If the annotation layer is bound to a module the annotations shall move when the module is moved.
   * This function shall receive the delta of the positions every time the module is moved.
   * @param type - Type of the movement. ("xAxis","yAxis" or "zAxis").
   * @param delta - Delta to the last position.
   */
  this.movementUpdate = function (type, delta) {
    var axis = -1;

    if (type === "xAxis") {
      axis = 0;
    }
    if (type === "yAxis") {
      axis = 1;
    }
    if (type === "zAxis") {
      axis = 2;
    }

    if (axis !== -1) {
      for (var i = 0; i < annotationTransforms.length; i++) {
        var trans = annotationTransforms[i].getAttribute("translation");
        var transValue = trans.split(" ");

        if (transValue.length < 3) {
          transValue = trans.split(",");
        }

        transValue[axis] = parseInt(transValue[axis]) - parseInt(delta);
        annotationTransforms[i].setAttribute("translation", transValue[0] + " " + transValue[1] + " " + transValue[2]);
      }
    }


  };

  /**
   * Adds an annotation marker and -text to the annotation layer.
   * @param xPos - Position on the X-Axis of the marker and center of the annotation.
   * @param yPos - Position on the Y-Axis of the marker and center of the annotation.
   * @param zPos - Position on the Z-Axis of the marker and center of the annotation.
   * @param Text - Text for the annotation.
   */
  this.addAnnotation = function (xPos, yPos, zPos, Text) {

    annotations.push(Text);//save the text for later queries

    //We draw 2 texts without their back faces.
    //So the user can see the text from most angles and not mirror inverted.
    for (var i = 0; i < 2; i++) {
      var textTransform = document.createElement('transform');
      textTransform.setAttribute('scale', fontSize + " " + fontSize + " " + fontSize);
      var shape = document.createElement('shape');
      var appearance = document.createElement('appearance');
      appearance.setAttribute("id", "Layer_Appearance_" + Name);
      var material = document.createElement('material');
      material.setAttribute('emissiveColor', fontColor);
      material.setAttribute('diffuseColor', fontColor);
      var text = document.createElement('text');
      text.setAttribute('string', Text);
      var fontStyle = document.createElement('fontStyle');
      fontStyle.setAttribute('family', 'calibri');
      fontStyle.setAttribute('style', 'bold');
      text.appendChild(fontStyle);
      appearance.appendChild(material);
      shape.appendChild(appearance);
      shape.appendChild(text);
      textTransform.appendChild(shape);

      //one marker is enough
      if (i === 0) {
        var sphere_trans = document.createElement("Transform");
        sphere_trans.setAttribute("scale", markerSize + " " + markerSize + " " + markerSize);
        sphere_trans.setAttribute('translation', xPos + " " + yPos + " " + zPos);
        var sphere_shape = document.createElement("Shape");
        var sphere = document.createElement("Sphere");
        var sphere_app = document.createElement("Appearance");
        var sphere_material = document.createElement('material');
        sphere_material.setAttribute('diffusecolor', markerColor);
        sphere_app.appendChild(sphere_material);
        sphere_shape.appendChild(sphere_app);
        sphere_shape.appendChild(sphere);
        sphere_trans.appendChild(sphere_shape);

        root.appendChild(sphere_trans);
        annotationTransforms.push(sphere_trans);

        sphere_trans = null;
        sphere_shape = null;
        sphere = null;
        sphere_app = null;
        sphere_material = null;
      }

      var rootTransform = document.createElement('transform');

      textTransform.setAttribute('translation', xPos + " " + (yPos + fontHover) + " " + zPos);
      textTransform.setAttribute('scale', (-fontSize) + " " + (-fontSize) + " " + fontSize);

      //One text "normal" and one "mirror inverted"
      if (i === 0) {
        textTransform.setAttribute('rotation', '0 0 1 3.14');
      }
      else {
        textTransform.setAttribute('rotation', '0 0 1 3.14');
        textTransform.setAttribute('translation', -xPos + " " + (yPos + fontHover) + " " + -zPos);
        rootTransform.setAttribute('rotation', '0 1 0 3.14');
      }

      annotationTransforms.push(rootTransform);//save the transform to toggle rendering
      rootTransform.appendChild(textTransform);
      root.appendChild(rootTransform);
    }

    textTransform = null;
    shape = null;
    appearance = null;
    material = null;
    text = null;
    fontStyle = null;
  };

  /**
   * Determine the rendering of this layer.
   * @param value - boolean
   */
  this.renderLayer = function (value) {
    for (var i = 0; i < annotationTransforms.length; i++) {
      annotationTransforms[i].setAttribute("render", value);
    }
  };


  /**
   * Returns an array with the annotation text.
   * @returns {Array}
   */
  this.getAnnotationTexts = function () {
    var arrayReturn = [];

    for (var i = 0; i < annotations.length; i++) {
      arrayReturn.push(annotations[i]);
    }

    return arrayReturn;
  };
};

/**
 * @class AxisLabels
 * @description This class generates labels for each axis and side (except bottom) of the bounding box.
 *
 * @param xSize - The width of the bounding box.
 * @param ySize - The height of the bounding box.
 * @param zSize - The depth of the bounding box.
 */
EarthServerGenericClient.AxisLabels = function (xSize, ySize, zSize) {
  /**
   * @description Defines the color of the text. Default at start: emissiveColor attribute is set, the diffuseColor one isn't.
   * @type {string}
   * @default "0.7 0.7 0.5"
   */
  var fontColor = "0.7 0.7 0.5";

  /**
   * @description Defines the size of the font. Value is always positive!
   * @default 50.0
   * @type {number}
   */
  var fontSize = 50.0;

  /**
   * @description Array stores all X3DOM transform nodes. Each transform contains the shape, material, text and fontStyle node.
   * @type {Array}
   * @default Empty
   */
  var transforms = [];
  /**
   * @description Array stores all text nodes of the x-axis.
   * @type {Array}
   * @default Empty
   */
  var textNodesX = [];
  /**
   * @description Array stores all text nodes of the y-axis.
   * @type {Array}
   * @default Empty
   */
  var textNodesY = [];
  /**
   * @description Array stores all text nodes of the z-axis.
   * @type {Array}
   * @default Empty
   */
  var textNodesZ = [];

  /**
   * @description This function changes the text size of each label independent of its axis.
   * @param size
   * The parameter (positive value expected) represents the desired size of the font.
   * Remember, the parameter represents the size in x3dom units not in pt like css.
   * Hence the size value could be large.
   */
  this.changeFontSize = function (size) {
    size = Math.abs(size);
    for (var i = 0; i < transforms.length; i++) {
      var scale = x3dom.fields.SFVec3f.parse(transforms[i].getAttribute('scale'));

      if (scale.x >= 0) scale.x = size; else scale.x = -1 * size;
      if (scale.y >= 0) scale.y = size; else scale.y = -1 * size;
      if (scale.z >= 0) scale.z = size; else scale.z = -1 * size;

      transforms[i].setAttribute('scale', scale.x + " " + scale.y + " " + scale.z);
    }
  };

  /**
   * This function changes the color of each label independent of its axis.
   * @param color
   * This parameter changes the current color value of each label.
   * It expects a string in x3d color format.
   * E.g. "1.0 1.0 1.0" for white and "0.0 0.0 0.0" for black.
   */
  this.changeColor = function (color) {
    for (var i = 0; i < transforms.length; i++) {
      var material = transforms[i].getElementsByTagName('material');

      for (var j = 0; j < material.length; j++) {
        material[j].setAttribute('emissiveColor', color);
        material[j].setAttribute('diffuseColor', color);
      }
    }
  };

  /**
   * @description This function changes the text of each label on the x-axis.
   * @param string
   * Defines the new text.
   */
  this.changeLabelNameX = function (string) {
    //Prevent multi line!
    while (string.search("'") != -1 || string.search("\"") != -1) {
      string = string.replace("'", " ");
      string = string.replace("\"", " ");
    }

    for (var i = 0; i < textNodesX.length; i++) {
      textNodesX[i].setAttribute('string', string);
    }
  };

  /**
   * @description This function changes the text of each label on the y-axis.
   * @param string
   * Defines the new text.
   */
  this.changeLabelNameY = function (string) {
    //Prevent multi line!
    while (string.search("'") != -1 || string.search("\"") != -1) {
      string = string.replace("'", " ");
      string = string.replace("\"", " ");
    }

    for (var i = 0; i < textNodesY.length; i++) {
      textNodesY[i].setAttribute('string', string);
    }
  };

  /**
   * @param string
   * Defines the new text.
   */
  this.changeLabelNameZ = function (string) {
    //Prevent multi line!
    while (string.search("'") != -1 || string.search("\"") != -1) {
      string = string.replace("'", " ");
      string = string.replace("\"", " ");
    }

    for (var i = 0; i < textNodesZ.length; i++) {
      textNodesZ[i].setAttribute('string', string);
    }
  };

  /**
   * @description This function generates labels on all three axis (x,y,z). The labels will be
   * added on each side (except bottom).
   */
  this.createAxisLabels = function (xLabel, yLabel, zLabel) {
    createLabel("x", "front", xLabel);
    createLabel("x", "back", xLabel);
    createLabel("x", "top", xLabel);

    createLabel("y", "front", yLabel);
    createLabel("y", "back", yLabel);
    createLabel("y", "left", yLabel);
    createLabel("y", "right", yLabel);

    createLabel("z", "front", zLabel);
    createLabel("z", "back", zLabel);
    createLabel("z", "top", zLabel);
  };

  /**
   * @description This (private) function creates the needed x3dom nodes.
   *
   * @param axis
   * Which axis do you want? Available: x, y, z
   *
   * @param side
   * Choose the side of the axis. <br>
   * Available for x: front (default), back and top. <br>
   * Available for y: front (default), back, left and right. <br>
   * Available for z: front (default), back and top.
   *
   * @param label
   * This text will appear at the given axis.
   */
  function createLabel(axis, side, label) {
    //Setup text
    var textTransform = document.createElement('transform');
    textTransform.setAttribute('scale', fontSize + " " + fontSize + " " + fontSize);
    var shape = document.createElement('shape');
    var appearance = document.createElement('appearance');
    var material = document.createElement('material');
    material.setAttribute('emissiveColor', fontColor);
    var text = document.createElement('text');
    text.setAttribute('string', label);
    var fontStyle = document.createElement('fontStyle');
    fontStyle.setAttribute('family', 'calibri');
    fontStyle.setAttribute('style', 'bold');
    text.appendChild(fontStyle);
    appearance.appendChild(material);
    shape.appendChild(appearance);
    shape.appendChild(text);
    textTransform.appendChild(shape);

    //var home = document.getElementById('x3dScene');
    var home = document.getElementById('AnnotationsGroup');
    var rotationTransform = document.createElement('transform');

    if (axis == "x") {
      textTransform.setAttribute('translation', "0 " + (ySize + fontSize / 2) + " " + zSize);

      if (side == "back") {
        rotationTransform.setAttribute('rotation', '0 1 0 3.14');
      }
      else if (side == "top") {
        textTransform.setAttribute('rotation', '1 0 0 -1.57');
        textTransform.setAttribute('translation', "0 " + -ySize + " " + (-zSize - fontSize / 2));
      }
      textNodesX[textNodesX.length] = text;
    }
    else if (axis == "y") {
      textTransform.setAttribute('translation', -(xSize + fontSize / 2) + " 0 " + zSize);
      textTransform.setAttribute('rotation', '0 0 1 1.57');

      if (side == "back") {
        textTransform.setAttribute('translation', (xSize + fontSize / 2) + " 0 " + zSize);
        textTransform.setAttribute('rotation', '0 0 1 4.74');
        rotationTransform.setAttribute('rotation', '1 0 0 3.14');
      }
      else if (side == "left") {
        rotationTransform.setAttribute('rotation', '0 1 0 -1.57');
      }
      else if (side == "right") {
        rotationTransform.setAttribute('rotation', '0 1 0 1.57');
      }
      textNodesY[textNodesY.length] = text;
    }
    else if (axis == "z") {
      textTransform.setAttribute('translation', xSize + " " + (ySize + fontSize / 2) + " 0");
      textTransform.setAttribute('rotation', '0 1 0 1.57');
      if (side == "back") {
        rotationTransform.setAttribute('rotation', '0 1 0 3.14');
      }
      else if (side == "top") {
        textTransform.setAttribute('rotation', '0 1 0 1.57');
        textTransform.setAttribute('translation', "0 0 0");

        rotationTransform.setAttribute('rotation', '0 0 1 -4.71');
        rotationTransform.setAttribute('translation', -(xSize + fontSize / 2) + " " + -ySize + " 0");
      }
      textNodesZ[textNodesZ.length] = text;
    }

    transforms[transforms.length] = textTransform;
    rotationTransform.appendChild(textTransform);
    home.appendChild(rotationTransform);
  }
};