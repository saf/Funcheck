%%%%%%%
% Reguły wnioskowania w systemie typow dla weryfikacji funkcyjności
%  metod i niemutowalności obiektów
%
%
% Reguły stanowią element pracy magisterskiej
%
%   "Weryfikacja funkcyjności metod Javy"
%
%                                     Sławomir Rudnicki 2011
%%%%%%%

% Deklarujemy operator :: ("jest typu")
%   Priorytet operatora jest równy priorytetowi operatorów
%   nierówności arytmetycznych oraz równości/uzgadnialności. 
:- op(700, xfx, ::).

% ok
program :: ok :-
	forall(class(C),
	  cl(C) :: ok
	  ;
	  (\+ cl(C) :: ok, print('Class fail~n'), fail)
	),
	!.
program :: nok.

% cok
cl(C) :: ok :-
	forall(method(M, C),
	  me(M) :: ok
	  ;
	  (\+ me(M) :: ok, print('Method fail~n'), fail)
	),
	!.
cl(_) :: nok.

% mok
me(M) :: ok :-
	forall(calls(M, Callee),
	  pure(Callee)
	  ;
	  (\+ pure(Callee), print('Invocation fail~n'), fail)
	),
	!.
me(_) :: nok.
