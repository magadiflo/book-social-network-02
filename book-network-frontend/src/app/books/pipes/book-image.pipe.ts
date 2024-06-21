import { Pipe, PipeTransform } from '@angular/core';

import { BookResponse } from '../../services/models';

@Pipe({
  name: 'bookImage',
  standalone: true
})
export class BookImagePipe implements PipeTransform {

  transform(book: BookResponse): string {
    return !!book.cover ? `data:image/jpg;base64,${book.cover}` : './assets/books/no_image_available.svg';
  }
}
