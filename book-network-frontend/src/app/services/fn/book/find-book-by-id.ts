/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';

import { BookResponse } from '../../models/book-response';

export interface FindBookById$Params {
  bookId: number;
}

export function findBookById(http: HttpClient, rootUrl: string, params: FindBookById$Params, context?: HttpContext): Observable<StrictHttpResponse<BookResponse>> {
  const rb = new RequestBuilder(rootUrl, findBookById.PATH, 'get');
  if (params) {
    rb.path('bookId', params.bookId, {});
  }

  return http.request(
    rb.build({ responseType: 'json', accept: 'application/json', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return r as StrictHttpResponse<BookResponse>;
    })
  );
}

findBookById.PATH = '/api/v1/books/{bookId}';
