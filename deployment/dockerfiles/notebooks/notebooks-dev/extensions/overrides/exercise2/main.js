/* tslint:disable:no-console */
/* tslint:disable:no-bitwise */
/* tslint:disable:no-empty */
define(['base/js/namespace', 'jquery', 'require', 'base/js/events'], function (IPython, $, requirejs, events) {
  'use strict';

  const gameConfig = {
    hostMode: false,
    userId: window.location.pathname.split('/')[3], // 'palasat', //
    participantApiKey: 'participant123',
    gameStarted: false,
    gameStopped: false,
    taskStarted: false,
    taskCompleted: false,
    solutionViewed: false,
    gameApiBaseUrl: window.origin + '/api/itsmmgame/event', // 'http://localhost:7171/api/itsmmgame/event', //
    timerId: null,
    elapsedTime: 0,
    exerciseCell: null,
    taskCellPresentInView: null,
    solutionCellPresentInView: null,
  };

  if (!gameConfig.hostMode) {
    getGameStatus();
    // $(window).focus(getGameStatus);
  }

  function getGameStatus() {
    setTimeout(function () {
      gameApiCall('status', (res) => {
        if (res.data) {
          if (res.data.gameStarted) {
            gameConfig.gameStarted = true;
            clearInterval(gameConfig.timerId);
          }
          switch (res.data.userEventStatus) {
            case 'NOTSTARTED':
              if (res.data.gameStopped) {
                gameConfig.gameStopped = true;
                get_Start_Task_CB(gameConfig.exerciseCell)();
              }
              break;
            case 'STARTED':
              gameConfig.taskStarted = true;
              gameConfig.solutionViewed = res.data.solutionSeen;
              get_Start_Task_CB(gameConfig.exerciseCell)();
              break;
            case 'STOPPED':
              setTaskCompletedState(res.data);
              break;
            default:
              break;
          }
        }
      });
    }, 300);
  }

  function gameApiCall(action, successCB, data) {
    const headers = { 'Content-Type': 'application/json', Accept: 'application/json' };
    let method = 'GET';
    let url = gameConfig.gameApiBaseUrl;
    let postData = data || {};
    switch (action) {
      case 'status':
        url += '/' + gameConfig.userId + '/info';
        headers.participantKey = gameConfig.participantApiKey;
        break;
      case 'start-task':
        method = 'POST';
        url += '/' + gameConfig.userId + '/start';
        headers.participantKey = gameConfig.participantApiKey;
        break;
      case 'view-solution':
        url += '/' + gameConfig.userId + '/answer';
        headers.participantKey = gameConfig.participantApiKey;
        break;
      case 'submit-answer':
        method = 'POST';
        url += '/' + gameConfig.userId + '/answer';
        headers.participantKey = gameConfig.participantApiKey;
        break;
      default:
        break;
    }
    $.ajax({
      url: url,
      headers: headers,
      type: method,
      dataType: 'json',
      data: data ? JSON.stringify(postData) : {},
      async: false,
      success: function (res) {
        successCB(res);
      },
      error: function (err) {
        console.log(err);
        infoModal('Action Failed!', 'Action Failed!. Please ty again or look into console for logs.');
      },
    });
  }

  var cfg = {
    add_button: false,
    use_hotkey: false,
    hotkey: 'Alt-D',
  };

  /**
   * handle click event
   *
   * @method click_solution_lock
   * @param evt {Event} jquery event
   */
  function click_solution_lock(evt) {
    if (!gameConfig.gameStarted) {
      infoModal(
        'Game not started!',
        'Game not started. You can do warm up tasks and refresh the page once game start time is annouced.',
      );
    }

    if (!gameConfig.solutionViewed && !gameConfig.gameStopped && !gameConfig.taskCompleted) {
      confirmModal(
        'Do you really want to view the solution?',
        'You will reduce your chances of winning, however you can always view the solution after submitting the answer.',
        showSolution,
        function () {},
        true,
      );

      evt.preventDefault();
      return false;
    } else if (gameConfig.solutionViewed || gameConfig.taskCompleted || gameConfig.gameStopped) {
      showSolutionCell();
    }
  }

  function showSolution() {
    gameApiCall('view-solution', (res) => {
      gameConfig.exerciseCell.element.find('input.onoffswitch-checkbox').prop('checked', true);
      showSolutionCell();
      gameConfig.solutionViewed = true;
    });
  }

  function showSolutionCell() {
    if (gameConfig.solutionCellPresentInView) {
      let cell = IPython.notebook.get_selected_cell();
      const isLocked = cell.metadata.solution2 === 'hidden';
      cell.metadata.solution2 = isLocked ? 'shown' : 'hidden';
      element_set_locked(cell, !isLocked);
      cell = IPython.notebook.get_next_cell(cell);
      while (cell !== null && cell.metadata.solution2 !== undefined && !cell.metadata.solution2_first) {
        cell.element.toggleClass('hidden', !isLocked);
        cell.metadata.solution2 = isLocked ? 'shown' : 'hidden';
        cell = IPython.notebook.get_next_cell(cell);
      }
    } else {
      const cell = IPython.notebook.get_selected_cell();
      const cellIndex = IPython.notebook.find_cell_index(cell);
      const solutionSource = ['# Solution\n', "data_task.groupby('Year_Month').sum().sort_values('Number').tail(1)"];
      const solutionCell = IPython.notebook.insert_cell_below('code', cellIndex);
      cell.metadata.solution2 = 'shown';
      solutionCell.metadata.solution2 = 'shown';
      solutionCell.metadata.isSolutionCell = true;
      solutionCell.set_text(solutionSource.join(''));
      // solutionCell.execute();
      gameConfig.solutionCellPresentInView = true;
    }
  }

  /**
   * Create or Remove an exercise in selected cells
   *
   * @method create_remove_exercise
   *
   */
  function create_remove_exercise() {
    var lcells = IPython.notebook.get_selected_cells();
    // It is possible that no cell is selected
    if (lcells.length < 1) {
      alert('Exercise extension:  \nPlease select some cells...');
      return;
    }

    var cell = lcells[0];
    if (cell.metadata.solution2_first) {
      remove_element(cell);
      delete cell.metadata.solution2_first;
      while (cell !== null && cell.metadata.solution2 !== undefined && !cell.metadata.solution2_first) {
        delete cell.metadata.solution2;
        cell.element.removeClass('hidden');
        cell = IPython.notebook.get_next_cell(cell);
      }
    } else {
      cell.metadata.solution2_first = true;
      cell.metadata.solution2 = 'hidden';
      add_element(cell);
      for (var k = 1; k < lcells.length; k++) {
        cell = lcells[k];
        cell.element.addClass('hidden');
        cell.metadata.solution2 = 'hidden';
      }
    }
  }

  function get_Start_Task_CB(cell) {
    return () => {
      if (!gameConfig.gameStarted) {
        gameApiCall('status', (res) => {
          if (res.data) {
            if (res.data.gameStarted) {
              gameConfig.gameStarted = true;
              clearInterval(gameConfig.timerId);
              get_Start_Task_CB(cell)();
            } else {
              infoModal('Game status!', 'Game has not started. Please wait...');
            }
          }
        });
        return;
      } else if (gameConfig.gameStopped) {
        infoModal('Game event closed!.', 'Game event has been closed by admin!. Better luck next time.');
        $(cell.element).find('.showSolutionActionsWrapper').addClass('showSection');
        $(cell.element).find('.taskBtnContainer').hide();
        $(cell.element).find('.input').show();
        $(cell.element).find('.answerWrapper').remove();
        $('.submitBtnContainer').remove();
        $('#timerWrapper').remove();
        renderTaskCell(cell);
        return false;
      }

      gameApiCall('start-task', (res) => {
        $(cell.element).find('.showSolutionActionsWrapper').addClass('showSection');
        $(cell.element).find('.taskBtnContainer').hide();
        $(cell.element).find('.input').show();

        setTimeout(function () {
          renderTaskCell(cell);

          if (res.data.gameStopped) {
            gameConfig.gameStopped = true;
            $(cell.element).find('.answerWrapper').remove();
            $(cell.element).find('.submitBtnContainer').remove();
            $('#timerWrapper').remove();
            infoModal('Game event closed!.', 'Game event has been closed by admin!. Better luck next time.');
          } else if (res.data.userEventStartTime && res.data.currentServerTime) {
            gameConfig.elapsedTime = new Date(res.data.currentServerTime) - new Date(res.data.userEventStartTime);
            $('#timerWrapper').show();
          }

          gameConfig.timerId = setInterval(function () {
            if (gameConfig.gameStopped) {
              clearInterval(gameConfig.timerId);
            } else {
              gameConfig.elapsedTime += 1000;
              $('#timeValue').text(msToTime(gameConfig.elapsedTime));
            }
          }, 1000);
        }, 300);
      });
    };
  }

  function setTaskCompletedState(data) {
    gameConfig.taskCompleted = true;
    const cell = gameConfig.exerciseCell;
    const timeTaken = new Date(data.userEventEndTime) - new Date(data.userEventStartTime);
    renderTaskCell(cell);
    $(cell.element).find('.showSolutionActionsWrapper').addClass('showSection');
    $(cell.element).find('.taskBtnContainer').hide();
    $(cell.element).find('.answerWrapper').html(`
      <h4>You have successfully completed the task.</h4>
      <h6>Submitted answer is - ${data.answer}</h6>
      <h6>Time taken is - ${msToTime(timeTaken)}</h6>
    `);
    $(cell.element).find('.input').show();
    $('.submitBtnContainer').remove();
    $('#timerWrapper').remove();
    clearInterval(gameConfig.timerId);
  }

  function renderTaskCell(cell) {
    if (gameConfig.taskCellPresentInView) {
      return;
    }
    const cellIndex = IPython.notebook.find_cell_index(cell);
    const taskText = [
      'As mentioned before the dataset contains number of clusters in CaaS in different availability zones over a period of time.\n',
      '\n',
      'Your task is to find out the month in which CaaS was operating with the highest number of clusters and the cluster count.\n',
      '\n',
      '**Hint:** *The month will correspond to the month when Germany said Good Bye to Euro 2021*\n',
      '\n',
    ];

    if (!gameConfig.gameStopped) {
      taskText.push(
        '**Note! When you press show solution button before submiting the answer, your winning chance will reduce**\n',
        '\n',
        '*Write your code in cell below and run the code cell before submitting.*',
      );
    }

    const taskCell = IPython.notebook.insert_cell_above('markdown', cellIndex);
    taskCell.metadata.isTaskCell = true;
    taskCell.set_text(taskText.join(''));
    taskCell.render();
    gameConfig.taskCellPresentInView = true;
  }

  function msToTime(s) {
    // Pad to 2 or 3 digits, default is 2
    var pad = (n, z = 2) => ('00' + n).slice(-z);
    return pad((s / 3.6e6) | 0) + ':' + pad(((s % 3.6e6) / 6e4) | 0) + ':' + pad(((s % 6e4) / 1000) | 0); //+ '.' + pad(s%1000, 3)
  }

  function get_Submit_CB(cell) {
    return () => {
      if (!gameConfig.gameStarted) {
        infoModal('Game status!', 'Game has not started. Please wait...');
        return;
      } else if (gameConfig.gameStopped) {
        infoModal('Game status!', 'Game has stopped. No more answer submission.');
        return false;
      }
      // if (!gameConfig.solutionViewed) {
      const cellJSON = cell.toJSON();
      const outputs = cellJSON.outputs;
      if (outputs.length) {
        const output = cellJSON.outputs[0].text;
        const source = cellJSON.source;
        const monthVal = $('#answerMonthSelect').val();
        const ansVal = $('#answerTextInput').val();
        if (monthVal !== '-1' && ansVal !== '' && !isNaN(ansVal)) {
          const answer = monthVal + ' - ' + parseInt(ansVal, 10);
          confirmModal(
            'Submit Answer',
            'Are you sure to submit following code and answer?\n\n' + source + '\n\n' + answer,
            function () {
              gameApiCall(
                'submit-answer',
                (res) => {
                  if (res.data.gameStopped) {
                    gameConfig.gameStopped = true;
                    $(cell.element).find('.answerWrapper').remove();
                    $(cell.element).find('.submitBtnContainer').remove();
                    $('#timerWrapper').remove();
                    infoModal('Game event closed!.', 'Game event has been closed by admin!. Better luck next time.');
                  } else {
                    setTaskCompletedState(res.data);
                  }
                },
                {
                  answer: answer,
                  codeSnippet: source,
                },
              );
            },
          );
        } else {
          infoModal(
            'Missing values!',
            'Please select year-month from the dropdown and enter the number of clusters in the text box.',
            function () {
              $('#answerTextInput').focus();
            },
          );
        }
      } else {
        infoModal('No output found!', 'We don’t see any code for the answer. Please enter the code and run the cell.');
      }
    };
  }

  /**
   *  Add a lock control to the given cell
   */
  var cbx = 0;
  function add_element(cell) {
    var ctrl = cell.element.find('.exercise');
    if (ctrl.length > 0) return ctrl;
    var locked = cell.metadata.solution2 === 'hidden';
    cell.element.css('flex-wrap', 'wrap');
    cbx += 1;
    ctrl = $(
      [
        '<div class="exercise exercise2">',
        '  <div class="prompt"></div>',
        '  <div class="taskBtnContainer">',
        '     <button id="startTaskBtn" type="submit">Start Task</button>',
        '  </div>',
        '  <div class="showSolutionActionsWrapper">',
        '    <div id="timerWrapper">',
        '      <label>Elapsed Time</label>',
        '      <h4 id="timeValue">00:00:00</h4>',
        '    </div>',
        '    <div class="answerWrapper">',
        '      <h6>Fill your answer in below fileds before submitting.</h6>',
        '      <select id="answerMonthSelect">',
        '        <option value="-1">Select month</option>',
        '        <option>2020-11</option>',
        '        <option>2020-12</option>',
        '        <option>2021-01</option>',
        '        <option>2021-02</option>',
        '        <option>2021-03</option>',
        '        <option>2021-04</option>',
        '        <option>2021-05</option>',
        '        <option>2021-06</option>',
        '      </select>',
        '      <input id="answerTextInput" type="number" maxlength="5" placeholder="# clusters" />',
        '    </div>',
        '    <div class="onoffswitch">',
        '      <input class="onoffswitch-checkbox" type="checkbox" id="myCheck' + cbx + '">',
        '      <label class="onoffswitch-label" for="myCheck' + cbx + '">',
        '        <div class="onoffswitch-inner"></div>',
        '        <div class="onoffswitch-switch"></div>',
        '      </label>',
        '    </div>',
        '    <div class="submitBtnContainer">',
        '      <button class="submitBtn" type="submit">',
        '       Submit Answer',
        '      </button>',
        '    </div>',
        '  </div>',
        '</div>',
      ].join('\n'),
    ).appendTo(cell.element);
    ctrl.find('#startTaskBtn').on('click', get_Start_Task_CB(cell));
    ctrl.find('input.onoffswitch-checkbox').on('click', click_solution_lock);
    ctrl.find('button.submitBtn').on('click', get_Submit_CB(cell));
    ctrl.find('#answerTextInput').keydown(function (evt) {
      evt.stopPropagation();
    });
    $(cell.element).find('.input').hide(); //code source area
    // element_set_locked(cell, locked);
    if (cell.metadata.solution2 === 'shown') {
      ctrl.find('input.onoffswitch-checkbox').prop('checked', true);
    }
    gameConfig.exerciseCell = cell;
    return ctrl;
  }

  function infoModal(titleText, message, okCallBack) {
    IPython.dialog.modal({
      title: titleText,
      body: $('<p/>').text(message),
      buttons: {
        Okay: {
          click: function () {
            if (okCallBack) {
              okCallBack();
            }
          },
        },
      },
    });
  }

  function confirmModal(titleText, message, okCallBack, cancelCallBack, isView) {
    const buttonsObj = {};

    if (isView) {
      buttonsObj.View = {
        click: function () {
          okCallBack();
        },
      };
    } else {
      buttonsObj.Okay = {
        click: function () {
          okCallBack();
        },
      };
    }

    buttonsObj.Cancel = {
      class: 'btn-danger',
      click: function () {
        cancelCallBack();
      },
    };

    IPython.dialog.modal({
      title: titleText,
      body: $('<p class="pre"/>').text(message),
      buttons: buttonsObj,
    });
  }

  function remove_element(cell) {
    cell.element.find('.exercise').remove();
  }

  function element_set_locked(cell, locked) {
    return cell.element.find('.exercise').prop('checked', !locked);
  }

  function refresh_exercises() {
    var inExercise = false;
    IPython.notebook.get_cells().forEach(function (cell) {
      if (inExercise && cell.metadata.solution2 !== undefined && !cell.metadata.solution2_first) {
        cell.element.toggleClass('hidden', cell.metadata.solution2 === 'hidden');
        if (!gameConfig.solutionCellPresentInView) {
          gameConfig.solutionCellPresentInView = cell.metadata.isSolutionCell;
        }
      } else {
        inExercise = false;
        if (!gameConfig.taskCellPresentInView) {
          gameConfig.taskCellPresentInView = cell.metadata.isTaskCell;
        }
      }
      if (!inExercise && cell.metadata.solution2 !== undefined) {
        inExercise = true;
        add_element(cell);
      }
    });
  }

  function load_ipython_extension() {
    // add css
    $('<link rel="stylesheet" type="text/css">').attr('href', requirejs.toUrl('./main.css')).appendTo('head');

    // Hide/display existing solutions at startup
    events.on('notebook_loaded.Notebook', refresh_exercises);
    if (IPython.notebook._fully_loaded) refresh_exercises();

    var actionName = IPython.keyboard_manager.actions.register(
      {
        help: 'Exercise2: Create/Remove exercise',
        help_index: 'ht',
        icon: 'fa-toggle-on',
        handler: create_remove_exercise,
      },
      'create_remove_exercise',
      'exercise2',
    );

    return IPython.notebook.config.loaded
      .then(function () {
        $.extend(true, cfg, IPython.notebook.config.data.exercise2);

        if (cfg.add_button) {
          IPython.toolbar.add_buttons_group([]);
        }
        if (cfg.use_hotkey && cfg.hotkey) {
          var cmdShrts = {};
          cmdShrts[cfg.hotkey] = actionName;
          IPython.keyboard_manager.command_shortcuts.add_shortcuts(cmdShrts);
        }
      })
      .catch(function (err) {
        console.warn('[exercise2] error:', err);
      });
  }

  return {
    load_ipython_extension: load_ipython_extension,
  };
});
