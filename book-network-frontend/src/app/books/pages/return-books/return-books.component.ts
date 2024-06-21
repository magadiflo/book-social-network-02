import { Component, inject } from '@angular/core';

import { BookService } from '../../../services/services';
import { BorrowedBookResponse, PageResponseBorrowedBookResponse } from '../../../services/models';

@Component({
  selector: 'app-return-books',
  standalone: true,
  imports: [],
  templateUrl: './return-books.component.html',
  styleUrl: './return-books.component.scss'
})
export default class ReturnBooksComponent {

  private _bookService = inject(BookService);

  public returnedBooks?: PageResponseBorrowedBookResponse;
  public message: string = '';
  public level: string = 'success';
  public page: number = 0;
  public size: number = 5;

  ngOnInit(): void {
    this.findAllReturnedBooks();
  }

  public findAllReturnedBooks(): void {
    this._bookService.findAllReturnedBooks({ page: this.page, size: this.size })
      .subscribe({
        next: pageResponseBorrowed => {
          console.log(pageResponseBorrowed);
          this.returnedBooks = pageResponseBorrowed;
        }
      })
  }

  public approveBookReturn(book: BorrowedBookResponse) {
    if (!book.returned) {
      return;
    }
    this._bookService.approvedReturnBorrowBook({ bookId: book.id! })
      .subscribe({
        next: bookTransactionHistoryId => {
          console.log({ bookTransactionHistoryId });
          this.message = 'Devolución del libro aprobada';
          this.level = 'success';
          this.findAllReturnedBooks();
        },
        error: err => {
          console.log(err);
          this.message = err.error.error;
          this.level = 'error';
        }
      });
  }

  public goToPage(page: number): void {
    this.page = page;
    this.findAllReturnedBooks();
  }

  public goToFirstPage(): void {
    this.page = 0;
    this.findAllReturnedBooks();
  }

  public goToLastPage(): void {
    this.page = this.returnedBooks?.totalPages as number - 1;
    this.findAllReturnedBooks();
  }

  public goToPreviousPage(): void {
    this.page--;
    this.findAllReturnedBooks();
  }

  public goToNextPage(): void {
    this.page++;
    this.findAllReturnedBooks();
  }

}
