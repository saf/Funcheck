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

error(Message, Key) :-
	format('~w@~w~n', [Message, Key]),
	fail.

% ok
program :: ok :-
	forall(class(C),
	  cl(C) :: ok
	),
	!.
program :: nok.

% cok
cl(C) :: ok :-
	forall(method(M, C),
	  me(M) :: ok
	),
	!.
cl(_) :: nok.

% mok
me(M) :: ok :-
	\+ pure(M).
me(M) :: ok :-
	pure(M), 
	forall(calls(Key, M, Callee),
	  pure(Callee)
	  ;
	  (\+ pure(Callee), error('impure.call', Key))
	),
	!.
me(_) :: nok.


