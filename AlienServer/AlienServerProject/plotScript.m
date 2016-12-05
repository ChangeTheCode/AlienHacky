close all
vals = dlmread('plotVals.csv')

graphics_toolkit("gnuplot")
plot( vals(:,1), vals(:,2) )
print -djpg -color plot
close all