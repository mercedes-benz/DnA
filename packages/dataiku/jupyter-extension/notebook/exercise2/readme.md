Custom Exercise2 extension
=========

The example below demonstrates some of the features of the exercise extensions.

- First, an solution or "details" cell is created by (a) selecting two cells with the rubberband and (b) clicking on the menu-button [exercise extension]
- Second, the two next cells are selected using a keyboard shortcut, and a solution is created using the shortcut Alt-D [exercise2 extension]
- Third, the two solutions are expanded by clicking on the corresponding widgets
- Fourth, the solutions are removed by selecting them and clicking on the buttons in the toolbar.




The extensions provide
----------------------

- a menubar button
- a sliding checkbox
- a submit button for submitting the result from the first cell after the hidden ones.


The menubar button is devoted to the creation or removing of the solution. The solution consists in several consecutive cells that can be selected by the usual notebook multicell selection methods (e.g. *Shift-down* (select next) or *Shift-up* (select previous) keyboard shortcuts, or using the rubberband extension.


### Creating a solution

Several cells being selected, pressing the menubar button adds a `cell widget` and hides the cells excepted the first one which serves as a heading cell. *Do not forget to keep the Shift key pressed down while clicking on the menu button
(otherwise selected cells will be lost)*. It is also possible to use a keyboard shortcut for creating the solution from selected cells: Alt-S for exercise extension and Alt-D for exercise2.


### Removing a solution

If a solution heading (first) cell is selected, then clicking the menu bar button removes this solution and its solutions cells are shown. Using the keyboard shortcut has the same effect.


### Showing/hiding solution

At creation of the solution, the solution cells are hidden. Clicking the `cell widget` toggles the hidden/shown state of the solution.


### Persistence

The state of solutions, hidden or shown, is preserved and automatically restored at startup and on reload.

