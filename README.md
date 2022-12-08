# ODE-Plotter
Plots given 2D ODEs and shows you the flows

.In order to use this, you should go to the 'manager' class and look for the function called 'odeInput'
.In here, the vector 'f_dot' can be changed to the (1st order, 2 dim) ODE you want to plot (using f.x as the first 
variable, and f.y as the second variable, where f_dot.x is the derivative with respect to t of the first variable 
and similarly for the f_dot.y and the 2nd variable)
.There is also a 'multi' function that affects the magnitude of the ODE
.Also, the variable 'bifur' can be included if you want an easily adjustable variable within the ODE

Note;
You can get cool effects by // out the 'displayBackground' function in 'displaySystem' (both in manager) so the flows are left as tracks 
rather than just seeing the points move individually