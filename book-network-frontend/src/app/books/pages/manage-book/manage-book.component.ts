import { Component, OnInit, inject } from '@angular/core';
import { FormGroup, NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, concatMap, filter, of, switchMap, tap } from 'rxjs';

import { BookService } from '../../../services/services';
import { BookRequest } from '../../../services/models';

type Action = 'create' | 'edit';

@Component({
  selector: 'app-manage-book',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './manage-book.component.html',
  styleUrl: './manage-book.component.scss'
})
export default class ManageBookComponent implements OnInit {

  private _router = inject(Router);
  private _activatedRoute = inject(ActivatedRoute);
  private _formBuilder = inject(NonNullableFormBuilder);
  private _bookService = inject(BookService);

  public form: FormGroup = this._formBuilder.group({
    id: [null],
    shareable: [false],
    authorName: [''],
    isbn: [''],
    synopsis: [''],
    title: [''],
  });
  public errorMessages: string[] = [];
  public selectedImageFile?: File;
  public imagePreview?: string;
  public action: Action = 'create';

  ngOnInit(): void {
    this._activatedRoute.params
      .pipe(
        filter(({ bookId }) => bookId),
        tap((_) => this.action = 'edit'),
        switchMap(({ bookId }) => this._bookService.findBookById({ bookId }))
      )
      .subscribe({
        next: bookResponse => {
          console.log(bookResponse);
          this.form.reset(bookResponse);

          if (bookResponse.cover) {
            this.imagePreview = `data:image/jpg;base64,${bookResponse.cover}`;
          }
        },
        error: err => {
          console.log(err);
          this._router.navigate(['/books', 'my-books']);
        }
      });
  }

  public onFileSelected(event: Event) {
    this.selectedImageFile = (event.target as HTMLInputElement).files![0];
    console.log(this.selectedImageFile);

    if (!this.selectedImageFile) {
      this.imagePreview = undefined;
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      this.imagePreview = reader.result as string;
    }
    reader.readAsDataURL(this.selectedImageFile);
  }

  public saveBook() {
    const request = this.form.value as BookRequest;
    this._bookService.saveBook({ body: request })
      .pipe(
        concatMap(bookId => this.selectedImageFile ? this.uploadImage(bookId) : of(bookId))
      )
      .subscribe({
        next: bookId => {
          console.log(bookId);
          this._router.navigate(['/books', 'my-books']);
        },
        error: err => {
          console.log(err);
          this.errorMessages = err.error.validationErrors;
        }
      });
  }

  public updateBook() {
    const request = this.form.value as BookRequest;
    this._bookService.updateBook({ body: request })
      .pipe(
        concatMap(bookId => this.selectedImageFile ? this.updateUploadImage(bookId) : of(bookId))
      )
      .subscribe({
        next: bookId => {
          console.log(bookId);
          this._router.navigate(['/books', 'my-books']);
        },
        error: err => {
          console.log(err);
          this.errorMessages = err.error.validationErrors;
        }
      });
  }

  private uploadImage(bookId: number): Observable<void> {
    return this._bookService.uploadBookCoverPicture({ bookId, body: { file: this.selectedImageFile! } });
  }

  private updateUploadImage(bookId: number): Observable<void> {
    return this._bookService.updateUploadBookCoverPicture({ bookId, body: { file: this.selectedImageFile! } });
  }

}
