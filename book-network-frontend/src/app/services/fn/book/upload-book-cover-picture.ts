/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { StrictHttpResponse } from '../../strict-http-response';
import { RequestBuilder } from '../../request-builder';


export interface UploadBookCoverPicture$Params {
  bookId: number;
      body?: {
'file': Blob;
}
}

export function uploadBookCoverPicture(http: HttpClient, rootUrl: string, params: UploadBookCoverPicture$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
  const rb = new RequestBuilder(rootUrl, uploadBookCoverPicture.PATH, 'post');
  if (params) {
    rb.path('bookId', params.bookId, {});
    rb.body(params.body, 'multipart/form-data');
  }

  return http.request(
    rb.build({ responseType: 'text', accept: '*/*', context })
  ).pipe(
    filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
    map((r: HttpResponse<any>) => {
      return (r as HttpResponse<any>).clone({ body: undefined }) as StrictHttpResponse<void>;
    })
  );
}

uploadBookCoverPicture.PATH = '/api/v1/books/cover/{bookId}';
