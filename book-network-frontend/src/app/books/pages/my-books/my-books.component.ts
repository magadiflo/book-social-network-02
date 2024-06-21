import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { BookService } from '../../../services/services';
import { BookResponse, PageResponseBookResponse } from '../../../services/models';
import { BookCardComponent } from '../../components/book-card/book-card.component';

@Component({
  selector: 'app-my-books',
  standalone: true,
  imports: [BookCardComponent, RouterLink],
  templateUrl: './my-books.component.html',
  styleUrl: './my-books.component.scss'
})
export default class MyBooksComponent implements OnInit {

  private _router = inject(Router);
  private _bookService = inject(BookService);

  public bookResponse?: PageResponseBookResponse;
  public page = 0;
  public size = 4;

  ngOnInit(): void {
    this.findAllBooks();
  }

  public findAllBooks() {
    this._bookService.findAllBooksByOwner({ page: this.page, size: this.size })
      .subscribe({
        next: pageBookResponse => {
          this.bookResponse = pageBookResponse;
          console.log(this.bookResponse);
        }
      });
  }

  public goToPage(page: number) {
    this.page = page;
    this.findAllBooks();
  }

  public goToFirstPage() {
    this.page = 0;
    this.findAllBooks();
  }

  public goToLastPage() {
    this.page = this.bookResponse?.totalPages as number - 1;
    this.findAllBooks();
  }

  public goToPreviousPage() {
    this.page--;
    this.findAllBooks();
  }

  public goToNextPage() {
    this.page++;
    this.findAllBooks();
  }

  public archiveBook(book: BookResponse) {
    this._bookService.updateArchivedStatus({ bookId: book.id! })
      .subscribe({
        next: bookId => {
          console.log(bookId);
          book.archived = !book.archived;
        }
      });
  }

  public shareBook(book: BookResponse) {
    this._bookService.updateShareableStatus({ bookId: book.id! })
      .subscribe({
        next: bookId => {
          console.log(bookId);
          book.shareable = !book.shareable;
        }
      });
  }

  public editBook(book: BookResponse) {
    this._router.navigate(['/books', 'manage', book.id]);
  }

}
