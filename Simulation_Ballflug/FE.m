function [y] = FE (x0, A, b, u, t_end, h, timeVector, C)

    x = zeros(size(x0, 1), size(timeVector,2));
    i = 1;
    x (:,i) = x0;
    
    for t = h:h:t_end
       dx_dt = A * x(:,i) + b*u;
       x(:, i + 1) = x(:, i) + dx_dt*h;
       i = i + 1;
       
    end

    y=C*x;
end