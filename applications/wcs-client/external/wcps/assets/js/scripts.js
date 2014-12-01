/**
 * List of constants used throughout the file
 */
Constants = {
  basePath: 'http://earthlook.org/'
}

/**
 * Checks if the browser is online and if not displays an error message
 */
function checkOnlineStatus() {
  var img = document.body.appendChild(document.createElement("img"));
  img.onload = function () {
    //nothing to do
  };
  img.onerror = function () {
    $.pnotify({
      title: 'Error',
      text : 'This demonstration requires internet access. Please connect and refresh the page.',
      type : 'error'
    })
  };
  img.src = "http://kahlua.eecs.jacobs-university.de/online.jpg";
}

jQuery(document).ready(function () {
  var isMenuHidden = true;
  var oldLeft;
  var menuLeft = $('.side-menu-container').css('left');
  var MENU_TIMEOUT = 1000;
  $(".side-menu-container").css("left", "-275px");

  if (window.location.href.match("demo")) {
    var menuLinks = [
      {
        isSection: true,
        title    : "BY SPATIOTEMPORAL DIMENSION"
      },
      {
        isSection: false,
        title    : "1 Dimensional data (1D)",
        url      : Constants.basePath + "/demo/spatio-temporal/1D.html"
      },
      {
        isSection: false,
        title    : "2 Dimensional data (2D)",
        url      : Constants.basePath + "/demo/spatio-temporal/2D.html"
      },
      {
        isSection: false,
        title    : "3 Dimensional data (3D)",
        url      : Constants.basePath + "/demo/spatio-temporal/3D.html"
      },
      {
        isSection: false,
        title    : "4 Dimensional data (4D)",
        url      : Constants.basePath + "/demo/spatio-temporal/4D.html"
      },
      {
        isSection: false,
        title    : "5 Dimensional data (5D)",
        url      : Constants.basePath + "/demo/spatio-temporal/5D.html"
      },
      {
        isSection: true,
        title    : "BY APPLICATION DOMAIN"
      },
      {
        isSection: false,
        title    : "Web mapping",
        url      : Constants.basePath + "/demo/application-domain/web-mapping.html"
      },
      {
        isSection: false,
        title    : "Remote sensing",
        url      : Constants.basePath + "/demo/application-domain/remote-sensing.html"
      },
      {
        isSection: false,
        title    : "Climatology",
        url      : Constants.basePath + "/demo/application-domain/climatology.html"
      },
      {
        isSection: false,
        title    : "Oceanography",
        url      : Constants.basePath + "/demo/application-domain/oceanography.html"
      },
      {
        isSection: false,
        title    : "Sensor timeseries",
        url      : Constants.basePath + "/demo/application-domain/sensor-timeseries.html"
      },
      {
        isSection: false,
        title    : "Life science",
        url      : Constants.basePath + "/demo/application-domain/life-science.html"
      },
      {
        isSection: true,
        title    : "BY GEO SERVICE STANDARD"
      },
      {
        isSection: false,
        title    : "WCPS",
        url      : Constants.basePath + "/demo/geo-service/wcps.html"
      },
      {
        isSection: false,
        title    : "WCS",
        url      : Constants.basePath + "/demo/geo-service/wcs.html"
      },
      {
        isSection: false,
        title    : "WMS",
        url      : Constants.basePath + "/demo/geo-service/wms.html"
      },
      {
        isSection: false,
        title    : "WPS",
        url      : Constants.basePath + "/demo/geo-service/wps.html"
      },
      {
        isSection: true,
        title    : "BY FUNCTIONALITY"
      },
      {
        isSection: false,
        title    : "Convolution",
        url      : Constants.basePath + "/demo/functionality/convolution.html"
      },
      {
        isSection: false,
        title    : "NDVI",
        url      : Constants.basePath + "/demo/functionality/ndvi.html"
      },
      {
        isSection: false,
        title    : "Data-retrieval",
        url      : Constants.basePath + "/demo/functionality/data-retrieval.html"
      },
      {
        isSection: false,
        title    : "Summarizing",
        url      : Constants.basePath + "/demo/functionality/summarizing.html"
      },
      {
        isSection: false,
        title    : "Point Clouds",
        url      : Constants.basePath + "/demo/functionality/point-clouds.html"
      },
      {
        isSection: true,
        title    : "BY CLIENT"
      },
      {
        isSection: false,
        title    : "3D Visual Client",
        url      : Constants.basePath + "/demo/client/3d-visual-client.html"
      },
      {
        isSection: false,
        title    : "Interactive Map",
        url      : Constants.basePath + "/demo/client/interactive-map.html"
      },
      {
        isSection: false,
        title    : "Query Sandbox",
        url      : Constants.basePath + "/demo/client/query-sandbox.html"
      }
    ];
    createMenu(".side-menu", menuLinks);
  }

  //set active links
  $('.side-menu-link').each(function () {
    if (window.location.href.match(this.href)) {
      $(this).parent().addClass("active");
      $(this).parent().parent().parent().addClass("in");
    }
  })
  $("#side-menu-collapse").on('click', function (e) {
    e.preventDefault();
    $this = $(this);
    var newTitle = $this.attr("data-alt-title");
    $this.attr("data-alt-title", $this.attr("data-original-title"));
    $this.attr("title", newTitle);
    if (isMenuHidden) {
      $(".side-menu").show();
      $(".side-menu-container").animate({'left': menuLeft}, MENU_TIMEOUT, function () {
      });
      isMenuHidden = false;
    }
    else {
      $(".side-menu-container").animate({'left': '-275px'}, MENU_TIMEOUT, function () {
        $(".side-menu").hide();
      });
      isMenuHidden = true;
    }
  })

  //pagination
  var nextUrl, prevUrl;
  var currentPage = $('.inside-link.active');
  var menuLength = $('.side-menu-link').length;

  if (currentPage.length == 0) {
    //demo page, prev = last menu link, next = first menu link
    nextUrl = $('.side-menu-link:first').attr('href');
    prevUrl = $('.side-menu-link:last').attr('href');
  }
  else {
    //inside the menu
    var currentPosition;
    //find out the current position in the menu
    for (var i = 0; i < menuLength; i++) {
      //console.log(currentPage.find('a').attr('href'), $($('.side-menu>li>a')[i]).attr('href'));
      if (currentPage.find('a').attr('href') == $($('.side-menu-link')[i]).attr('href')) {
        currentPosition = i;
        break;
      }
    }
    //console.log("not first page, postion is:", currentPosition);
    if (currentPosition == 0) {
      //first page, next is second, prev is last
      nextUrl = $($('.side-menu-link')[1]).attr('href');
      prevUrl = $('.side-menu-link:last').attr('href');
    }
    else if (currentPosition == menuLength - 1) {
      //last page, next is first page, prev is the one before last
      nextUrl = $('.side-menu-link:first').attr('href');
      prevUrl = $($('.side-menu-link')[menuLength - 2]).attr('href');
    }
    else {

      //any page in the middle, next and prev are straight forward
      prevUrl = $($('.side-menu-link')[currentPosition - 1]).attr('href');
      nextUrl = $($('.side-menu-link')[currentPosition + 1]).attr('href');
    }
  }

  if (prevUrl && nextUrl) {
    $($('#pagination-li-prev').find('a')).attr('href', prevUrl);
    $($('#pagination-li-next').find('a')).attr('href', nextUrl);
  }
});

/**
 * Creates the left side expandable menu
 * @param links Array[{title: string, url: string, isSection: bool}] the menu links
 */
var createMenu = function (selector, links) {
  var menuHtml =
    "<div class='accordion' id='accordion2'>";
  var currentResult = "";
  var counter = -1;
  for (var i = 0; i < links.length; i++) {
    if (links[i].isSection == true) {
      counter++;
      if (counter) {
        currentResult += "  </div>" +
          "      </div>" +
          "    </div>";
      }
      currentResult +=
        "  <div class='accordion-group'>" +
          "    <div class='accordion-heading'>" +
          "      <a class='accordion-toggle' data-toggle='collapse' data-parent='#accordion2' href='#collapse" + counter + "'>" +
          "          <div class='section-link'><i class='icon-play-circle'></i><span class='link-in-class'>" + links[i].title + "</span></div>" +
          "      </a>" +
          "    </div>" +
          "    <div id='collapse" + counter + "' class='accordion-body collapse'>" +
          "      <div class='accordion-inner'>";
      //if previous section exists append </div>
    }
    else {
      currentResult +=
        "        <div class='inside-link'><a class='side-menu-link' href='" + links[i].url + "'><i class='icon-chevron-right'></i> " + links[i].title + "</a></div>";

    }
  }

  menuHtml += currentResult +
    "  </div>" +
    "</div>";

  $(selector).html(menuHtml);
}


jQuery(document).ready(function(){
  jQuery("title").text("Big Earth Data Standards");
  jQuery("div.nav-collapse ul.nav li:nth-child(2) a").html("<i class='icon-book'></i> <br/>About");
  jQuery(".logo-motto a").html("<h1>Big Earth Data Standards</h1><p>build your own spatio-temporal dataset</p>");
})