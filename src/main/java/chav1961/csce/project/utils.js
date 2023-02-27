/*
<query>::=<orQuery>
<orQuery>::=<andQuery>[ OR <andQuery> ...]
<andQuery>::=<notQuery>[ and <notQuery> ...]
<notQuery>::= [-]{<singleQuery>|<rangeQuery>}
<rangeQuery>::='['<value> TO <value>']'
<singleQuery>::={<value>|<sequence>|<wildCard>|(<orQuery>)}[^<boost>]
<value>::={<name>|<number>}
<name>::=<letter>[<alphanum>...]
<sequence>::="<any_except_dquote>"[~<proximity>]
<proximity>::=<number>
<wildCard>::=<letter>[{<alphanum>|*|?>...]
<boost>::=<number>[.<number>]
*/
