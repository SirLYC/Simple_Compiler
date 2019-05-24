begin
  整数integer k;
  integer iam变量
  integer 1k;
  integer function F(n);
    begin
      integer n;
      if n >< 0 then F : n*F(n-1)
      else F = 1
    end;
  read(m);
  k : F(m);
  write(k)
end
