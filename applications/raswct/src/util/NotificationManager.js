/**
 * @brief Description
 * @author: Alex Dumitru <alex@flanche.net>
 * @package pack
 */

FlancheJs.defineClass("Rj.util._NotificationManager", {

  init:function () {

  },

  properties:{
    enabled:{
      value:true
    }
  },

  methods:{
    disable:function () {
      this.setEnabled(true)
    },
    enable :function () {
      this.setEnabled(false);
    },

    notify:function (title, body, persistent) {
      this._createNotification(title, body, persistent);
    },

    alert:function (title, message, buttonText, extraClasses) {
      buttonText = buttonText || "Ok";
      extraClasses = extraClasses || "";
      this._alert(title, message, buttonText, extraClasses);
    }
  },

  internals:{
    createNotification:function (title, body, persistent) {
      var $ = jQuery;
      // Make it a window property see we can call it outside via updateGrowls() at any point
      var updateGrowls = function () {
        // Loop over each jGrowl qTip
        var each = $('.qtip.jgrowl'),
          width = each.outerWidth(),
          height = each.outerHeight(),
          gap = each.eq(0).qtip('option', 'position.adjust.y'),
          pos;

        each.each(function (i) {
          var api = $(this).data('qtip');

          // Set target to window for first or calculate manually for subsequent growls
          api.options.position.target = !i ? $(window) : [
            pos.left + width, pos.top + (height * i) + Math.abs(gap * (i - 1))
          ];
          api.set('position.at', 'top right');

          // If this is the first element, store its finak animation position
          // so we can calculate the position of subsequent growls above
          if (!i) {
            pos = api.cache.finalPos;
          }
        });
      };

      // Setup our timer function
      var timer = function (event) {
        var api = $(this).data('qtip'),
          lifespan = 5000; // 5 second lifespan

        // If persistent is set to true, don't do anything.
        if (api.get('show.persistent') === true) {
          return;
        }

        // Otherwise, start/clear the timer depending on event type
        clearTimeout(api.timer);
        if (event.type !== 'mouseover') {
          api.timer = setTimeout(api.hide, lifespan);
        }
      }
      // Use the last visible jGrowl qtip as our positioning target
      var target = $('.qtip.jgrowl:visible:last');

      // Create your jGrowl qTip...
      $(document.body).qtip({
        // Any content config you want here really.... go wild!
        content :{
          text :body,
          title:{
            text  :title,
            button:true
          }
        },
        position:{
          my    :'top right',
          // Not really important...
          at    :(target.length ? 'bottom' : 'top') + ' right',
          // If target is window use 'top right' instead of 'bottom right'
          target:target.length ? target : $(window),
          // Use our target declared above
          adjust:{ y:5 },
          effect:function (api, newPos) {
            // Animate as usual if the window element is the target
            $(this).animate(newPos, {
              duration:200,
              queue   :false
            });

            // Store the final animate position
            api.cache.finalPos = newPos;
          }
        },
        show    :{
          event     :false,
          // Don't show it on a regular event
          ready     :true,
          // Show it when ready (rendered)
          effect    :function () {
            $(this).stop(0, 1).fadeIn(400);
          },
          // Matches the hide effect
          delay     :0,
          // Needed to prevent positioning issues
          // Custom option for use with the .get()/.set() API, awesome!
          persistent:persistent
        },
        hide    :{
          event :false,
          // Don't hide it on a regular event
          effect:function (api) {
            // Do a regular fadeOut, but add some spice!
            $(this).stop(0, 1).fadeOut(400).queue(function () {
              // Destroy this tooltip after fading out
              api.destroy();

              // Update positions
              updateGrowls();
            })
          }
        },
        style   :{
          classes:'jgrowl ui-tooltip-dark ui-tooltip-rounded',
          // Some nice visual classes
          tip    :false // No tips for this one (optional ofcourse)
        },
        events  :{
          render:function (event, api) {
            // Trigger the timer (below) on render
            timer.call(api.elements.tooltip, event);
          }
        }
      }).removeData('qtip');

      $(document).delegate('.qtip.jgrowl', 'mouseover mouseout', timer);

    },

    alert:function (title, message, buttonText, extraClasses) {
      var $ = jQuery;
      /*
       * Common dialogue() function that creates our dialogue qTip.
       * We'll use this method to create both our prompt and confirm dialogues
       * as they share very similar styles, but with varying content and titles.
       */
      var dialogue = function (content, title) {
        /*
         * Since the dialogue isn't really a tooltip as such, we'll use a dummy
         * out-of-DOM element as our target instead of an actual element like document.body
         */
        $('<div />').qtip(
          {
            content :{
              text :content,
              title:title
            },
            position:{
              my    :'center', at:'center', // Center it...
              target:$(window) // ... in the window
            },
            show    :{
              ready:true, // Show it straight away
              modal:{
                on  :true, // Make it modal (darken the rest of the page)...
                blur:false // ... but don't close the tooltip when clicked
              }
            },
            hide    :false, // We'll hide it maunally so disable hide events
            style   :'ui-tooltip-light ui-tooltip-rounded ui-tooltip-dialogue raswct-alert-message ' + extraClasses, // Add a few styles
            events  :{
              // Hide the tooltip when any buttons in the dialogue are clicked
              render:function (event, api) {
                $('button', api.elements.content).click(api.hide);
              },
              // Destroy the tooltip once it's hidden as we no longer need it!
              hide  :function (event, api) {
                api.destroy();
              }
            }
          });
      }

      var Alert = function (title, message, buttonText) {
        // Content will consist of the message and an ok button
        var message = $('<p />', { text:message }),
          ok = $('<button />', { text:buttonText, 'class':'full' });
        dialogue(message.add(ok), title);
      }

      Alert(title, message, buttonText);
    }
  }
})

Rj.util.NotificationManager = new Rj.util._NotificationManager();