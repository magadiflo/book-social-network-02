import { Component, OnInit, inject } from '@angular/core';
import { FormGroup, NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';

import { BookService, FeedbackService } from '../../../services/services';
import { BorrowedBookResponse, FeedbackRequest, PageResponseBorrowedBookResponse } from '../../../services/models';
import { RatingComponent } from '../../components/rating/rating.component';

@Component({
  selector: 'app-borrowed-book-list',
  standalone: true,
  imports: [ReactiveFormsModule, RatingComponent],
  templateUrl: './borrowed-book-list.component.html',
  styleUrl: './borrowed-book-list.component.scss'
})
export default class BorrowedBookListComponent implements OnInit {

  private _bookService = inject(BookService);
  private _feedbackService = inject(FeedbackService);
  private _formBuilder = inject(NonNullableFormBuilder);

  public form: FormGroup = this._formBuilder.group({
    bookId: [null],
    note: [0],
    comment: [''],
  });
  public borrowedBooks?: PageResponseBorrowedBookResponse;
  public selectedBook?: BorrowedBookResponse;
  public page: number = 0;
  public size: number = 5;

  ngOnInit(): void {
    this.findAllBorrowedBooks();
  }

  public findAllBorrowedBooks(): void {
    this._bookService.findAllBorrowedBooks({ page: this.page, size: this.size })
      .subscribe({
        next: pageResponseBorrowed => {
          console.log(pageResponseBorrowed);
          this.borrowedBooks = pageResponseBorrowed;
        }
      })
  }

  public returnBorrowedBook(book: BorrowedBookResponse): void {
    this.form.get('bookId')?.patchValue(book.id);
    this.selectedBook = book;
    console.log(book);
  }

  public returnBook(withFeedback: boolean): void {
    this._bookService.returnBorrowBook({ bookId: this.selectedBook?.id! })
      .subscribe({
        next: bookTransactionHistoryId => {
          if (withFeedback) {
            this.giveFeedback();
          }
          this.selectedBook = undefined;
          this.findAllBorrowedBooks();
        }
      });
  }

  public goToPage(page: number): void {
    this.page = page;
    this.findAllBorrowedBooks();
  }

  public goToFirstPage(): void {
    this.page = 0;
    this.findAllBorrowedBooks();
  }

  public goToLastPage(): void {
    this.page = this.borrowedBooks?.totalPages as number - 1;
    this.findAllBorrowedBooks();
  }

  public goToPreviousPage(): void {
    this.page--;
    this.findAllBorrowedBooks();
  }

  public goToNextPage(): void {
    this.page++;
    this.findAllBorrowedBooks();
  }

  private giveFeedback(): void {
    this._feedbackService.saveFeedback({ body: this.form.value as FeedbackRequest })
      .subscribe({
        next: feedbackId => {
          console.log({ feedbackId });
        }
      });
  }
}

